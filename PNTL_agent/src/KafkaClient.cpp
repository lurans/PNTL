
#include <sstream>

using namespace std;

#include "Sal.h"
#include "AgentCommon.h"
#include "Log.h"

#include "KafkaClient.h"


// 操作kafka时需要互斥, 确保多线程操作安全
// 消息发送是异步进行的, 互斥锁对于性能影响应该不大
#define KAFKA_LOCK() \
        if (stKafkaLock) \
            sal_mutex_take(stKafkaLock, sal_mutex_FOREVER)
            
#define KAFKA_UNLOCK() \
        if (stKafkaLock) \
            sal_mutex_give(stKafkaLock)
            
// kafka log Level
#define LOG_EMERG   0
#define LOG_ALERT   1
#define LOG_CRIT    2
#define LOG_ERR     3
#define LOG_WARNING 4
#define LOG_NOTICE  5
#define LOG_INFO    6
#define LOG_DEBUG   7

// kafka 日志回调, 用于接管kafka日志
void KafkaLogger (const rd_kafka_t *pstKafka, INT32 iLevel,
    const char *pcFac, const char *pcBuf) 
{
    if (iLevel <= LOG_ERR)
    {
        KAFKA_CLIENT_ERROR("RDKAFKA-[%i]-[%s]: [%s]: [%s]",
            iLevel, pcFac, pstKafka ? rd_kafka_name(pstKafka) : NULL, pcBuf);
    }
    else if (iLevel <= LOG_WARNING)
    {
        KAFKA_CLIENT_WARNING("RDKAFKA-[%i]-[%s]: [%s]: [%s]",
            iLevel, pcFac, pstKafka ? rd_kafka_name(pstKafka) : NULL, pcBuf);
    }
    else
    {
        KAFKA_CLIENT_INFO("RDKAFKA-[%i]-[%s]: [%s]: [%s]",
            iLevel, pcFac, pstKafka ? rd_kafka_name(pstKafka) : NULL, pcBuf);
    }
}

// kafka 发送报告回调
/**
 * Message delivery report callback.
 * Called once for each message.
 * See rdkafka.h for more information.
 */
void KafkaDeliveredReport(rd_kafka_t *rk,
                void *payload, size_t len,
                rd_kafka_resp_err_t error_code,
                void *opaque, void *msg_opaque) 
{
    if (error_code)
    {
        // 消息发送失败
        
        char *  pacLogBuffer = NULL; 
        pacLogBuffer = new char[len + 1];
        if (pacLogBuffer)
        {
            sal_memset(pacLogBuffer, 0, (len + 1));
            // 确保访问不越界
            strncpy(pacLogBuffer, (const char*)payload, len);
            
            KAFKA_CLIENT_WARNING("Message delivery failed[%s], Payload[%u][%s]", 
                rd_kafka_err2str(error_code), len, pacLogBuffer);

            delete [] pacLogBuffer;
            pacLogBuffer = NULL;
        }
        else
            KAFKA_CLIENT_WARNING("Message delivery failed[%s], Payload len[%u], and No Enough Memory",
                rd_kafka_err2str(error_code), len);
    }
}

// 构造函数, 所有成员初始化默认值.
KafkaClient_C::KafkaClient_C()
{
    KAFKA_CLIENT_INFO("Creat a new KafkaClient");
    
    // 互斥锁保护
    stKafkaLock = NULL;
    // kafka句柄
    pstKafka = NULL;
    // 默认为Producer
    eClientType = KAFAKA_CLIENT_TYPE_PRODUCER;

    // Broker List
    KafkaBrokerList.clear();
    
    // kafka topic句柄, 一个topic属于特定kafka实例.
    KafkaTopicList.clear();

    // 默认使用分区
    /**
     * @brief Unassigned partition.
     *
     * The unassigned partition is used by the producer API for messages
     * that should be partitioned using the configured or default partitioner.
     */
    iDefaultPartition = RD_KAFKA_PARTITION_UA;
}

// 析构函数,释放资源
KafkaClient_C::~KafkaClient_C()
{
    KAFKA_CLIENT_INFO("Destroy an old KafkaClient");
    
    StopKafkaClient();

    if (stKafkaLock)
        sal_mutex_destroy(stKafkaLock);
    stKafkaLock = NULL;
}

// 添加broker服务器地址信息.
INT32 KafkaClient_C::AddBrokerAddress(string strNewBrokerInfo)
{
    
    
    // kafka client 已经启动, 不允许修改配置
    if (pstKafka)
    {
        KAFKA_CLIENT_ERROR("Can't add broker server when kafka client is running");
        return AGENT_E_PARA;
    }
    
    // 添加broker会在init前调用, 申请互斥锁.
    if (NULL == stKafkaLock)
    {
        stKafkaLock = sal_mutex_create("Kafka Client Lock");
        if( NULL == stKafkaLock )
        {
            KAFKA_CLIENT_ERROR("Create mutex failed");
            return AGENT_E_MEMORY;
        }
    }

    KAFKA_CLIENT_INFO("Add a new broker[%s] to kafka client", strNewBrokerInfo.c_str());

    KAFKA_LOCK();
    vector<string>::iterator pstrKafkaBrokerInfo;
    for(pstrKafkaBrokerInfo = KafkaBrokerList.begin(); pstrKafkaBrokerInfo != KafkaBrokerList.end(); pstrKafkaBrokerInfo++)
    {
        if (strNewBrokerInfo == *pstrKafkaBrokerInfo)
        {
            KAFKA_UNLOCK();
            MSG_CLIENT_WARNING("Broker server address already exist");
            return AGENT_E_EXIST;
        }
    }
    KafkaBrokerList.push_back(strNewBrokerInfo);
    KAFKA_UNLOCK();
    
    return AGENT_OK;
}

// 初始化kafka句柄, 前提是已经配置了broker列表
INT32 KafkaClient_C::StartKafkaClient(KafakClientType_E eNewClientType)
{
    INT32 iRet = AGENT_OK;
    char acKafkaErr[512];
    
    rd_kafka_type_t eKafkaType;
    switch (eNewClientType)
    {
        case KAFAKA_CLIENT_TYPE_PRODUCER:
            eKafkaType = RD_KAFKA_PRODUCER;
            break;
            
        case KAFAKA_CLIENT_TYPE_CONSUMER:
            eKafkaType = RD_KAFKA_CONSUMER;
            break;
            
        default:
            KAFKA_CLIENT_ERROR("Unknown client type[%d]", eNewClientType);
            return AGENT_E_PARA;
    }

    
    if (0 == KafkaBrokerList.size())
    {
        KAFKA_CLIENT_ERROR("Broker list is empty, call AddBrokerAddress first");
        return AGENT_E_PARA;
    }

    KAFKA_CLIENT_INFO("Start librdkafka[ver:%s] client as a [%s(%d)]", rd_kafka_version_str(), (eNewClientType ? "CONSUMER":"PRODUCER"), eNewClientType);

    KAFKA_LOCK();
    // Create Kafka handle
    if (NULL == pstKafka)
    {
        rd_kafka_conf_res_t stKafkaConfRet;
        // kafka句柄配置
        rd_kafka_conf_t *pstKafkaConf = NULL;
        
        // 参数中可识别的Broker数量
        UINT32 uiBrokerNumber = 0;
        
        // Kafka configuration 
        pstKafkaConf = rd_kafka_conf_new();
        if (NULL == pstKafkaConf) 
        {
            KAFKA_UNLOCK();
            KAFKA_CLIENT_ERROR("Failed to create new Kafka configuration");
            return AGENT_E_ERROR;
        }
        // 如果需要补充kafka全局配置,可以在此添加
        if (RD_KAFKA_PRODUCER == eKafkaType)
        {
            // producer发送队列最大缓存时间, 单位ms, 默认值为1s, 取值范围1-900*1000ms.
            // 消息最大缓存时间到达后, 即使只有一个消息也立刻启动发送, 控制最大时延.
            // 默认5分钟(基于topic的message.timeout.ms设定)内未发送成功的,会触发发送失败, 回调发送报告(DR)
            stKafkaConfRet = rd_kafka_conf_set(pstKafkaConf, "queue.buffering.max.ms", "500",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_conf_destroy(pstKafkaConf);
                pstKafkaConf = NULL;

                KAFKA_UNLOCK();
                KAFKA_CLIENT_ERROR("Failed to set new queue.buffering.max.ms, type[%d],err[%s]", eNewClientType, acKafkaErr);
                return AGENT_E_ERROR;
            }


            // 日志是否记录与Broker的连接断开事件, bool变量, 默认值为1(记录), 取值范围 0 或者 1.
            // 客户端与每一个Broker之间保持一个连接, 对于0.9版本以上的Broker服务器, 一段时间内没有收到消息后会主动断开连接, 触发大量错误记录
            // 0.9版本以上Broker服务器, 默认 10 分钟无连接则端口连接, 连接断开后客户端会重新连接, 业务不会受损.
            // 此选项可以关闭连接断开时记录日志功能.
            stKafkaConfRet = rd_kafka_conf_set(pstKafkaConf, "log.connection.close", "0",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_conf_destroy(pstKafkaConf);
                pstKafkaConf = NULL;

                KAFKA_UNLOCK();
                KAFKA_CLIENT_ERROR("Failed to set new log.connection.close, type[%d],err[%s]", eNewClientType, acKafkaErr);
                return AGENT_E_ERROR;
            }

#if 0
            // producer发送队列长度(消息个数), 默认值100000个, 取值范围1 - 10000000个;
            // 超过上限后新增消息时会返回full错误.
            stKafkaConfRet = rd_kafka_conf_set(pstKafkaConf, "queue.buffering.max.messages", "100",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_conf_destroy(pstKafkaConf);
                pstKafkaConf = NULL;

                KAFKA_UNLOCK();
                KAFKA_CLIENT_ERROR("Failed to set new queue.buffering.max.messages, type[%d],err[%s]", eNewClientType, acKafkaErr);
                return AGENT_E_ERROR;
            }
            
            // producer错误消息发送失败后最大重试次数, 默认值2次, 取值范围0 - 10000000 次.
            // How many times to retry sending a failing MessageSet.
            stKafkaConfRet = rd_kafka_conf_set(pstKafkaConf, "message.send.max.retries", "5",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_conf_destroy(pstKafkaConf);
                pstKafkaConf = NULL;

                KAFKA_UNLOCK();
                KAFKA_CLIENT_ERROR("Failed to set new message.send.max.retries, type[%d],err[%s]", eNewClientType, acKafkaErr);
                return AGENT_E_ERROR;
            }

            // producer消息发送失败后retry前的等待时间, 单位ms, 默认值100ms, 取值范围1 - 300*1000 ms.
            // The backoff time in milliseconds before retrying a message send.

            stKafkaConfRet = rd_kafka_conf_set(pstKafkaConf, "retry.backoff.ms", "500",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_conf_destroy(pstKafkaConf);
                pstKafkaConf = NULL;

                KAFKA_UNLOCK();
                KAFKA_CLIENT_ERROR("Failed to set new retry.backoff.ms, type[%d],err[%s]", eNewClientType, acKafkaErr);
                return AGENT_E_ERROR;
            }
#endif

        }

        // 接管kafka消息发送报告
        rd_kafka_conf_set_dr_cb(pstKafkaConf, KafkaDeliveredReport);

        // 错误回调使用log记录.
        //If no error_cb is registered then the errors will be logged instead.
        //rd_kafka_conf_set_error_cb(pstKafkaConf, error_cb);
        
        // 接管kafka日志
        rd_kafka_conf_set_log_cb(pstKafkaConf, KafkaLogger);
        
        // 使用配置信息创建kafka.
        pstKafka = rd_kafka_new(eKafkaType, pstKafkaConf, acKafkaErr, sizeof(acKafkaErr));
        if (NULL == pstKafka) 
        {   
            rd_kafka_conf_destroy(pstKafkaConf);
            pstKafkaConf = NULL;

            KAFKA_UNLOCK();
            KAFKA_CLIENT_ERROR("Failed to create new kafka, type[%d],err[%s]", eNewClientType, acKafkaErr);
            return AGENT_E_ERROR;
        }
        // kafka创建成功, conf对象已经被使用(实际上是copy后销毁), 不可再次使用.
        pstKafkaConf = NULL;

        // 打印详细信息
        rd_kafka_set_log_level(pstKafka, LOG_DEBUG);
        
        // 添加broker
        stringstream ssStringBrokerList;
        // 遍历broker list
        vector<string>::iterator pstrKafkaBrokerInfo;
        for(pstrKafkaBrokerInfo = KafkaBrokerList.begin(); pstrKafkaBrokerInfo != KafkaBrokerList.end(); pstrKafkaBrokerInfo++)
        {
            ssStringBrokerList << pstrKafkaBrokerInfo->c_str() <<",";
        }
        
        uiBrokerNumber = rd_kafka_brokers_add(pstKafka, ssStringBrokerList.str().c_str());
        if (0 == uiBrokerNumber)
        {
            rd_kafka_destroy(pstKafka);
            pstKafka = NULL;

            KAFKA_UNLOCK();
            KAFKA_CLIENT_ERROR("No valid brokers specified");
            return AGENT_E_ERROR;
        }
    }
    else
    {
        KAFKA_UNLOCK();
        KAFKA_CLIENT_ERROR("kafka client is running and don't restart this client");
        return AGENT_E_PARA;
    }
    
    KAFKA_UNLOCK();
    iRet = StartThread();
    if(iRet)
    {
        KAFKA_CLIENT_ERROR("StartThread failed[%d]", iRet);
        return iRet;
    }
    return AGENT_OK;
}

// 销毁kafka句柄, 包括所有topic句柄.
INT32 KafkaClient_C::StopKafkaClient()
{
    INT32 iRet = AGENT_OK;

    KAFKA_CLIENT_INFO("Stop a [%s(%d)] kafka client", (eClientType ? "CONSUMER":"PRODUCER"), eClientType);
    
    KAFKA_LOCK();
    
    // Destroy topic 
    vector<KafakTopicEntry_S>::iterator pcKafakTopicEntry;
    for(pcKafakTopicEntry = KafkaTopicList.begin(); pcKafakTopicEntry != KafkaTopicList.end(); pcKafakTopicEntry++)
    {
        if ( pcKafakTopicEntry->pstKafkaTopic )
        {
            rd_kafka_topic_destroy(pcKafakTopicEntry->pstKafkaTopic);
            pcKafakTopicEntry->pstKafkaTopic = NULL;
        }
    }
    KafkaTopicList.clear();

    iRet = StopThread();
    if(iRet)
    {
        KAFKA_UNLOCK();
        KAFKA_CLIENT_ERROR("StopThread failed[%d]", iRet);
        return iRet;
    }
    
    // Destroy kafka handle
    if (pstKafka)
        rd_kafka_destroy(pstKafka);
    pstKafka = NULL;

    // Destroy Broker List
    KafkaBrokerList.clear();

    KAFKA_UNLOCK();
    return AGENT_OK;
}

// 根据topic name 创建新的topic句柄
INT32 KafkaClient_C::AddNewTopic(string * pstrTopicName, rd_kafka_topic_t ** ppstKafkaTopic)
{
    (* ppstKafkaTopic) = NULL;

    // topic从属于特定kafka句柄
    if (NULL == pstKafka)
    {
        KAFKA_CLIENT_ERROR("Should Init Kafka First");
        return AGENT_E_ERROR;
    }
    // 创建新的topic.
    KafakTopicEntry_S stKafkaTopicEntry;
    stKafkaTopicEntry.strTopicName = *pstrTopicName;

    KAFKA_CLIENT_INFO("Add a new topic [%s] to kafka client", stKafkaTopicEntry.strTopicName.c_str());
    {
        // topic句柄配置
        rd_kafka_topic_conf_t *pstTopicConf = NULL;
        
        // Topic configuration
        pstTopicConf = rd_kafka_topic_conf_new();
        if (NULL == pstTopicConf) 
        { 
            KAFKA_CLIENT_ERROR("Failed to create new Topic configuration");
            return AGENT_E_ERROR;
        }

        /* 如果需要补充topic配置,可以在此添加 */
#if 0
        {
            rd_kafka_conf_res_t stKafkaConfRet;
            char acKafkaErr[512];
            
            // producer消息本地老化时间(基于topic配置), 单位ms, 默认值5分钟, 取值范围0 - 900*1000ms. 0表示无限制
            // 超时后会将消息从发送队列中移除,并触发发送失败
            stKafkaConfRet = rd_kafka_topic_conf_set(pstTopicConf, "message.timeout.ms", "60000",
                        acKafkaErr, sizeof(acKafkaErr));
            if ( RD_KAFKA_CONF_OK != stKafkaConfRet)
            {
                rd_kafka_topic_conf_destroy(pstTopicConf);
                pstTopicConf = NULL;
                
                KAFKA_CLIENT_ERROR("Failed to set new message.timeout.ms for topic[%s], err[%s]", 
                    stKafkaTopicEntry.strTopicName.c_str(), acKafkaErr);
                return AGENT_E_ERROR;
            }
        }
#endif

        // 根据配置信息创建topic
        stKafkaTopicEntry.pstKafkaTopic = rd_kafka_topic_new(pstKafka, stKafkaTopicEntry.strTopicName.c_str(), pstTopicConf);
        if (NULL == stKafkaTopicEntry.pstKafkaTopic)
        {
            rd_kafka_topic_conf_destroy(pstTopicConf);
            pstTopicConf = NULL;
            
            KAFKA_CLIENT_ERROR("Failed to create new topic: [%s]", stKafkaTopicEntry.strTopicName.c_str());
            return AGENT_E_ERROR;
        }
        // topic 创建成功, conf对象已经被使用(实际上是copy后销毁), 不可再次使用.
        pstTopicConf = NULL;
    }
        
    KafkaTopicList.push_back(stKafkaTopicEntry);
    
    *ppstKafkaTopic = stKafkaTopicEntry.pstKafkaTopic;
    
    return AGENT_OK;
}

// 根据topic name查找对应topic 句柄
INT32 KafkaClient_C::SearchTopicList(string * pstrTopicName, rd_kafka_topic_t ** ppstKafkaTopic)
{
    * ppstKafkaTopic = NULL;
    
    vector<KafakTopicEntry_S>::iterator pcKafakTopicEntry;
    for(pcKafakTopicEntry = KafkaTopicList.begin(); pcKafakTopicEntry != KafkaTopicList.end(); pcKafakTopicEntry++)
    {
        if (   ((pcKafakTopicEntry->strTopicName) == (*pstrTopicName))
            && (pcKafakTopicEntry->pstKafkaTopic))
        {
            * ppstKafkaTopic = pcKafakTopicEntry->pstKafkaTopic;
            return AGENT_OK;
        }
    }

    return AGENT_E_NOT_FOUND;
}

// 根据topic name查找对应topic句柄, 如果没找到则创建一个.
INT32 KafkaClient_C::GetTopic(string * pstrTopicName, rd_kafka_topic_t ** ppstKafkaTopic)
{
    INT32 iRet = AGENT_E_NOT_FOUND;
    
    * ppstKafkaTopic = NULL;
    
    // 根据topic name查找对应topic 句柄
    iRet = SearchTopicList(pstrTopicName, ppstKafkaTopic);
    if (AGENT_E_NOT_FOUND == iRet)
    {
        KAFKA_CLIENT_INFO("Can't find topic[%s], create it now", pstrTopicName->c_str());
        // 创建新的topic.
        iRet = AddNewTopic(pstrTopicName, ppstKafkaTopic);
        if (iRet)
        {
            KAFKA_CLIENT_ERROR("Init New Topic failed[%d]: [%s]", iRet, pstrTopicName->c_str());
            return iRet;
        }
    }
    return iRet;
}

// 向kafka发送消息
INT32 KafkaClient_C::ProduceKafkaMsg(string* pstrTopicName, string* pstrMsg)
{
    INT32 iRet = AGENT_OK;
    rd_kafka_topic_t * pstKafkaTopic = NULL;

    if (NULL == pstKafka)
    {
        KAFKA_CLIENT_ERROR("Should Init Kafka First");
        return AGENT_E_PARA;
    }
    if (KAFAKA_CLIENT_TYPE_PRODUCER != eClientType)
    {
        KAFKA_CLIENT_ERROR("Kafka client type is[%d], can't produce msg", eClientType);
        return AGENT_E_PARA;
    }
   
    //KAFKA_CLIENT_INFO("Produce Data:[%s]", pstrMsg->c_str());

    KAFKA_LOCK();
    iRet = GetTopic(pstrTopicName, &pstKafkaTopic);
    if (iRet)
    {
        KAFKA_UNLOCK();
        KAFKA_CLIENT_ERROR("Get Topic[%s] Failed[%d]", pstrTopicName->c_str(), iRet);
        return AGENT_E_ERROR;
    }

    // 加入信息与发送信息默认是异步的, 使用互斥锁保护对性能影响应该不大.
    iRet = rd_kafka_produce(pstKafkaTopic, iDefaultPartition, 
                    RD_KAFKA_MSG_F_COPY, 
                    /* Payload and length */
                    (void *)(pstrMsg->c_str()), pstrMsg->size(),
                    /* Optional key and its length */
                    NULL, 0,
                    /* Message opaque, provided in
                    * delivery report callback as
                    * msg_opaque. */
                    NULL);
    if (iRet)
    {
        /* Poll to handle delivery reports */
        rd_kafka_poll(pstKafka, 0);
        
        KAFKA_UNLOCK();
        KAFKA_CLIENT_ERROR("Failed to produce to topic[%s], partition [%i]: %s",
            rd_kafka_topic_name(pstKafkaTopic), iDefaultPartition,
            rd_kafka_err2str(rd_kafka_last_error()));
        return AGENT_E_ERROR;
    }

    /* Poll to handle delivery reports */
    rd_kafka_poll(pstKafka, 0);
    
    KAFKA_UNLOCK();
    
    return AGENT_OK;
}

// 从kafka接收消息, 待实现
INT32 KafkaClient_C::ConsumeKafkaMsg(string* pstrTopicName, string* pstrMsg)
{
    
    if (NULL == pstKafka)
    {
        KAFKA_CLIENT_ERROR("Should Init Kafka First");
        return AGENT_E_PARA;
    }
    if (KAFAKA_CLIENT_TYPE_CONSUMER != eClientType)
    {
        KAFKA_CLIENT_ERROR("Kafka client type is[%d], can't consume msg", eClientType);
        return AGENT_E_PARA;
    }
    KAFKA_LOCK();
    /* 待实现 */
    KAFKA_UNLOCK();
    
    KAFKA_CLIENT_ERROR("ConsumeKafkaMsg not available now");
    return AGENT_E_ERROR;
}


// Thread回调函数.
// PreStopHandler()执行后, ThreadHandler()需要在GetCurrentInterval() us内主动退出.
// 用于定期调用 rd_kafka_poll(pstKafka, 100);
INT32 KafkaClient_C::ThreadHandler()
{
    KAFKA_CLIENT_INFO("Kakfka Polling Task Start Working, Interval[%d]", GetCurrentInterval());
    while (GetCurrentInterval())
    {
        if (   pstKafka
            && (0 < rd_kafka_outq_len(pstKafka)))
        {
            // 快速触发dr等回调函数, 时间单位为ms
            /* Poll to handle delivery reports */
            rd_kafka_poll(pstKafka, (GetCurrentInterval()/MILLISECOND_USEC));
        }
        else
            sal_usleep(GetCurrentInterval());
    }
    KAFKA_CLIENT_INFO("Kakfka Polling Task Exiting, Interval[%d]", GetCurrentInterval());
    return AGENT_OK;
}

// Thread即将启动, 通知ThreadHandler做好准备.
INT32 KafkaClient_C::PreStartHandler()
{
    
    return AGENT_OK;
}

// Thread即将停止, 通知ThreadHandler主动退出.
INT32 KafkaClient_C::PreStopHandler()
{
    SetNewInterval(0);
    return AGENT_OK;
}


