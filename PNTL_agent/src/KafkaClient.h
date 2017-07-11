#ifndef __SRC_KafkaClient_H__
#define __SRC_KafkaClient_H__

#include <librdkafka/rdkafka.h>
#include <string>
#include <vector>

#include "ThreadClass.h"


typedef enum tagKafakClientType
{
    KAFAKA_CLIENT_TYPE_PRODUCER = 0,     // Producer, 向kafka服务器发送消息
    KAFAKA_CLIENT_TYPE_CONSUMER,         // Consumer, 从kafka服务器读取消息
    KAFAKA_CLIENT_TYPE_MAX
} KafakClientType_E;

// Topic List Entry
typedef struct tagKafakTopicEntry
{
    // topic 句柄
    rd_kafka_topic_t * pstKafkaTopic;
    // topic 名称
    string strTopicName;
} KafakTopicEntry_S;


// KafkaClient类定义, 负责启动和管理KafkaClient服务.
// KafkaClient依赖librdkafka.
class KafkaClient_C : ThreadClass_C
{
private:
    sal_mutex_t stKafkaLock;                   // 操作kafka client时需要互斥.

    // kafka句柄
    rd_kafka_t * pstKafka;

    // 对象类型
    KafakClientType_E eClientType;

    // Broker List
    vector <string> KafkaBrokerList;

    // kafka topic句柄, 一个topic属于特定kafka实例.
    vector <KafakTopicEntry_S> KafkaTopicList;

    // 默认使用分区
    INT32 iDefaultPartition;

    INT32 AddNewTopic(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // 根据topic name 创建新的topic句柄
    INT32 SearchTopicList(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // 根据topic name查找对应topic 句柄
    INT32 GetTopic(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // 根据topic name查找对应topic句柄, 如果没找到则创建一个

    /* Thread 实现代码 */
    INT32 ThreadHandler();                            // 任务主处理函数
    INT32 PreStopHandler();                           // StopThread触发, 通知ThreadHandler主动退出.
    INT32 PreStartHandler();                          // StartThread触发, 通知ThreadHandler即将被调用.


public:
    KafkaClient_C();                                    // 构造函数, 填充默认值.
    ~KafkaClient_C();                                   // 析构函数, 释放必要资源.

    INT32 AddBrokerAddress(
        string strNewBrokerInfo);              // 添加broker服务器地址信息.
    INT32 StartKafkaClient(
        KafakClientType_E eNewClientType);          // 初始化kafka句柄, 启动kafka任务. 需要提前添加broker.
    INT32 StopKafkaClient();                              // 停止kafka任务, 销毁kafka句柄, 包括所有topic句柄.

    INT32 ProduceKafkaMsg(string* pstrTopicName, string* pstrMsg); // 向kafka发送消息
    INT32 ConsumeKafkaMsg(string* pstrTopicName, string* pstrMsg); // 从kafka接收消息, 待实现
};

#endif

