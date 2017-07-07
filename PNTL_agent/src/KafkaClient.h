#ifndef __SRC_KafkaClient_H__
#define __SRC_KafkaClient_H__

#include <librdkafka/rdkafka.h>
#include <string>
#include <vector>

#include "ThreadClass.h"


typedef enum tagKafakClientType
{
    KAFAKA_CLIENT_TYPE_PRODUCER = 0,     // Producer, ��kafka������������Ϣ
    KAFAKA_CLIENT_TYPE_CONSUMER,         // Consumer, ��kafka��������ȡ��Ϣ
    KAFAKA_CLIENT_TYPE_MAX
} KafakClientType_E;

// Topic List Entry
typedef struct tagKafakTopicEntry
{
    // topic ���
    rd_kafka_topic_t * pstKafkaTopic;
    // topic ����
    string strTopicName;
} KafakTopicEntry_S;


// KafkaClient�ඨ��, ���������͹���KafkaClient����.
// KafkaClient����librdkafka.
class KafkaClient_C : ThreadClass_C
{
private:
    sal_mutex_t stKafkaLock;                   // ����kafka clientʱ��Ҫ����.

    // kafka���
    rd_kafka_t * pstKafka;

    // ��������
    KafakClientType_E eClientType;

    // Broker List
    vector <string> KafkaBrokerList;

    // kafka topic���, һ��topic�����ض�kafkaʵ��.
    vector <KafakTopicEntry_S> KafkaTopicList;

    // Ĭ��ʹ�÷���
    INT32 iDefaultPartition;

    INT32 AddNewTopic(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // ����topic name �����µ�topic���
    INT32 SearchTopicList(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // ����topic name���Ҷ�Ӧtopic ���
    INT32 GetTopic(
        string * pstrTopicName,
        rd_kafka_topic_t ** ppstKafkaTopic); // ����topic name���Ҷ�Ӧtopic���, ���û�ҵ��򴴽�һ��

    /* Thread ʵ�ִ��� */
    INT32 ThreadHandler();                            // ������������
    INT32 PreStopHandler();                           // StopThread����, ֪ͨThreadHandler�����˳�.
    INT32 PreStartHandler();                          // StartThread����, ֪ͨThreadHandler����������.


public:
    KafkaClient_C();                                    // ���캯��, ���Ĭ��ֵ.
    ~KafkaClient_C();                                   // ��������, �ͷű�Ҫ��Դ.

    INT32 AddBrokerAddress(
        string strNewBrokerInfo);              // ���broker��������ַ��Ϣ.
    INT32 StartKafkaClient(
        KafakClientType_E eNewClientType);          // ��ʼ��kafka���, ����kafka����. ��Ҫ��ǰ���broker.
    INT32 StopKafkaClient();                              // ֹͣkafka����, ����kafka���, ��������topic���.

    INT32 ProduceKafkaMsg(string* pstrTopicName, string* pstrMsg); // ��kafka������Ϣ
    INT32 ConsumeKafkaMsg(string* pstrTopicName, string* pstrMsg); // ��kafka������Ϣ, ��ʵ��
};

#endif

