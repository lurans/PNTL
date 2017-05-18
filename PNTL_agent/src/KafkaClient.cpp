
#include <sstream>

using namespace std;

#include "Sal.h"
#include "AgentCommon.h"
#include "Log.h"

#include "KafkaClient.h"


// ����kafkaʱ��Ҫ����, ȷ�����̲߳�����ȫ
// ��Ϣ�������첽���е�, ��������������Ӱ��Ӧ�ò���
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

// kafka ��־�ص�, ���ڽӹ�kafka��־
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

// kafka ���ͱ���ص�
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
        // ��Ϣ����ʧ��
        
        char *  pacLogBuffer = NULL; 
        pacLogBuffer = new char[len + 1];
        if (pacLogBuffer)
        {
            sal_memset(pacLogBuffer, 0, (len + 1));
            // ȷ�����ʲ�Խ��
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

// ���캯��, ���г�Ա��ʼ��Ĭ��ֵ.
KafkaClient_C::KafkaClient_C()
{
    KAFKA_CLIENT_INFO("Creat a new KafkaClient");
    
    // ����������
    stKafkaLock = NULL;
    // kafka���
    pstKafka = NULL;
    // Ĭ��ΪProducer
    eClientType = KAFAKA_CLIENT_TYPE_PRODUCER;

    // Broker List
    KafkaBrokerList.clear();
    
    // kafka topic���, һ��topic�����ض�kafkaʵ��.
    KafkaTopicList.clear();

    // Ĭ��ʹ�÷���
    /**
     * @brief Unassigned partition.
     *
     * The unassigned partition is used by the producer API for messages
     * that should be partitioned using the configured or default partitioner.
     */
    iDefaultPartition = RD_KAFKA_PARTITION_UA;
}

// ��������,�ͷ���Դ
KafkaClient_C::~KafkaClient_C()
{
    KAFKA_CLIENT_INFO("Destroy an old KafkaClient");
    
    StopKafkaClient();

    if (stKafkaLock)
        sal_mutex_destroy(stKafkaLock);
    stKafkaLock = NULL;
}

// ���broker��������ַ��Ϣ.
INT32 KafkaClient_C::AddBrokerAddress(string strNewBrokerInfo)
{
    
    
    // kafka client �Ѿ�����, �������޸�����
    if (pstKafka)
    {
        KAFKA_CLIENT_ERROR("Can't add broker server when kafka client is running");
        return AGENT_E_PARA;
    }
    
    // ���broker����initǰ����, ���뻥����.
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

// ��ʼ��kafka���, ǰ�����Ѿ�������broker�б�
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
        // kafka�������
        rd_kafka_conf_t *pstKafkaConf = NULL;
        
        // �����п�ʶ���Broker����
        UINT32 uiBrokerNumber = 0;
        
        // Kafka configuration 
        pstKafkaConf = rd_kafka_conf_new();
        if (NULL == pstKafkaConf) 
        {
            KAFKA_UNLOCK();
            KAFKA_CLIENT_ERROR("Failed to create new Kafka configuration");
            return AGENT_E_ERROR;
        }
        // �����Ҫ����kafkaȫ������,�����ڴ����
        if (RD_KAFKA_PRODUCER == eKafkaType)
        {
            // producer���Ͷ�����󻺴�ʱ��, ��λms, Ĭ��ֵΪ1s, ȡֵ��Χ1-900*1000ms.
            // ��Ϣ��󻺴�ʱ�䵽���, ��ʹֻ��һ����ϢҲ������������, �������ʱ��.
            // Ĭ��5����(����topic��message.timeout.ms�趨)��δ���ͳɹ���,�ᴥ������ʧ��, �ص����ͱ���(DR)
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


            // ��־�Ƿ��¼��Broker�����ӶϿ��¼�, bool����, Ĭ��ֵΪ1(��¼), ȡֵ��Χ 0 ���� 1.
            // �ͻ�����ÿһ��Broker֮�䱣��һ������, ����0.9�汾���ϵ�Broker������, һ��ʱ����û���յ���Ϣ��������Ͽ�����, �������������¼
            // 0.9�汾����Broker������, Ĭ�� 10 ������������˿�����, ���ӶϿ���ͻ��˻���������, ҵ�񲻻�����.
            // ��ѡ����Թر����ӶϿ�ʱ��¼��־����.
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
            // producer���Ͷ��г���(��Ϣ����), Ĭ��ֵ100000��, ȡֵ��Χ1 - 10000000��;
            // �������޺�������Ϣʱ�᷵��full����.
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
            
            // producer������Ϣ����ʧ�ܺ�������Դ���, Ĭ��ֵ2��, ȡֵ��Χ0 - 10000000 ��.
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

            // producer��Ϣ����ʧ�ܺ�retryǰ�ĵȴ�ʱ��, ��λms, Ĭ��ֵ100ms, ȡֵ��Χ1 - 300*1000 ms.
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

        // �ӹ�kafka��Ϣ���ͱ���
        rd_kafka_conf_set_dr_cb(pstKafkaConf, KafkaDeliveredReport);

        // ����ص�ʹ��log��¼.
        //If no error_cb is registered then the errors will be logged instead.
        //rd_kafka_conf_set_error_cb(pstKafkaConf, error_cb);
        
        // �ӹ�kafka��־
        rd_kafka_conf_set_log_cb(pstKafkaConf, KafkaLogger);
        
        // ʹ��������Ϣ����kafka.
        pstKafka = rd_kafka_new(eKafkaType, pstKafkaConf, acKafkaErr, sizeof(acKafkaErr));
        if (NULL == pstKafka) 
        {   
            rd_kafka_conf_destroy(pstKafkaConf);
            pstKafkaConf = NULL;

            KAFKA_UNLOCK();
            KAFKA_CLIENT_ERROR("Failed to create new kafka, type[%d],err[%s]", eNewClientType, acKafkaErr);
            return AGENT_E_ERROR;
        }
        // kafka�����ɹ�, conf�����Ѿ���ʹ��(ʵ������copy������), �����ٴ�ʹ��.
        pstKafkaConf = NULL;

        // ��ӡ��ϸ��Ϣ
        rd_kafka_set_log_level(pstKafka, LOG_DEBUG);
        
        // ���broker
        stringstream ssStringBrokerList;
        // ����broker list
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

// ����kafka���, ��������topic���.
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

// ����topic name �����µ�topic���
INT32 KafkaClient_C::AddNewTopic(string * pstrTopicName, rd_kafka_topic_t ** ppstKafkaTopic)
{
    (* ppstKafkaTopic) = NULL;

    // topic�������ض�kafka���
    if (NULL == pstKafka)
    {
        KAFKA_CLIENT_ERROR("Should Init Kafka First");
        return AGENT_E_ERROR;
    }
    // �����µ�topic.
    KafakTopicEntry_S stKafkaTopicEntry;
    stKafkaTopicEntry.strTopicName = *pstrTopicName;

    KAFKA_CLIENT_INFO("Add a new topic [%s] to kafka client", stKafkaTopicEntry.strTopicName.c_str());
    {
        // topic�������
        rd_kafka_topic_conf_t *pstTopicConf = NULL;
        
        // Topic configuration
        pstTopicConf = rd_kafka_topic_conf_new();
        if (NULL == pstTopicConf) 
        { 
            KAFKA_CLIENT_ERROR("Failed to create new Topic configuration");
            return AGENT_E_ERROR;
        }

        /* �����Ҫ����topic����,�����ڴ���� */
#if 0
        {
            rd_kafka_conf_res_t stKafkaConfRet;
            char acKafkaErr[512];
            
            // producer��Ϣ�����ϻ�ʱ��(����topic����), ��λms, Ĭ��ֵ5����, ȡֵ��Χ0 - 900*1000ms. 0��ʾ������
            // ��ʱ��Ὣ��Ϣ�ӷ��Ͷ������Ƴ�,����������ʧ��
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

        // ����������Ϣ����topic
        stKafkaTopicEntry.pstKafkaTopic = rd_kafka_topic_new(pstKafka, stKafkaTopicEntry.strTopicName.c_str(), pstTopicConf);
        if (NULL == stKafkaTopicEntry.pstKafkaTopic)
        {
            rd_kafka_topic_conf_destroy(pstTopicConf);
            pstTopicConf = NULL;
            
            KAFKA_CLIENT_ERROR("Failed to create new topic: [%s]", stKafkaTopicEntry.strTopicName.c_str());
            return AGENT_E_ERROR;
        }
        // topic �����ɹ�, conf�����Ѿ���ʹ��(ʵ������copy������), �����ٴ�ʹ��.
        pstTopicConf = NULL;
    }
        
    KafkaTopicList.push_back(stKafkaTopicEntry);
    
    *ppstKafkaTopic = stKafkaTopicEntry.pstKafkaTopic;
    
    return AGENT_OK;
}

// ����topic name���Ҷ�Ӧtopic ���
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

// ����topic name���Ҷ�Ӧtopic���, ���û�ҵ��򴴽�һ��.
INT32 KafkaClient_C::GetTopic(string * pstrTopicName, rd_kafka_topic_t ** ppstKafkaTopic)
{
    INT32 iRet = AGENT_E_NOT_FOUND;
    
    * ppstKafkaTopic = NULL;
    
    // ����topic name���Ҷ�Ӧtopic ���
    iRet = SearchTopicList(pstrTopicName, ppstKafkaTopic);
    if (AGENT_E_NOT_FOUND == iRet)
    {
        KAFKA_CLIENT_INFO("Can't find topic[%s], create it now", pstrTopicName->c_str());
        // �����µ�topic.
        iRet = AddNewTopic(pstrTopicName, ppstKafkaTopic);
        if (iRet)
        {
            KAFKA_CLIENT_ERROR("Init New Topic failed[%d]: [%s]", iRet, pstrTopicName->c_str());
            return iRet;
        }
    }
    return iRet;
}

// ��kafka������Ϣ
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

    // ������Ϣ�뷢����ϢĬ�����첽��, ʹ�û���������������Ӱ��Ӧ�ò���.
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

// ��kafka������Ϣ, ��ʵ��
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
    /* ��ʵ�� */
    KAFKA_UNLOCK();
    
    KAFKA_CLIENT_ERROR("ConsumeKafkaMsg not available now");
    return AGENT_E_ERROR;
}


// Thread�ص�����.
// PreStopHandler()ִ�к�, ThreadHandler()��Ҫ��GetCurrentInterval() us�������˳�.
// ���ڶ��ڵ��� rd_kafka_poll(pstKafka, 100);
INT32 KafkaClient_C::ThreadHandler()
{
    KAFKA_CLIENT_INFO("Kakfka Polling Task Start Working, Interval[%d]", GetCurrentInterval());
    while (GetCurrentInterval())
    {
        if (   pstKafka
            && (0 < rd_kafka_outq_len(pstKafka)))
        {
            // ���ٴ���dr�Ȼص�����, ʱ�䵥λΪms
            /* Poll to handle delivery reports */
            rd_kafka_poll(pstKafka, (GetCurrentInterval()/MILLISECOND_USEC));
        }
        else
            sal_usleep(GetCurrentInterval());
    }
    KAFKA_CLIENT_INFO("Kakfka Polling Task Exiting, Interval[%d]", GetCurrentInterval());
    return AGENT_OK;
}

// Thread��������, ֪ͨThreadHandler����׼��.
INT32 KafkaClient_C::PreStartHandler()
{
    
    return AGENT_OK;
}

// Thread����ֹͣ, ֪ͨThreadHandler�����˳�.
INT32 KafkaClient_C::PreStopHandler()
{
    SetNewInterval(0);
    return AGENT_OK;
}


