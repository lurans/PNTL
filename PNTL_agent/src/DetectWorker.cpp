#include <errno.h>

#include <sys/time.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "AgentCommon.h"

#define UDP_TEST_PORT       6000
#define UDP_SERVER_IP       "127.0.0.1"

using namespace std;

#include "Log.h"
#include "DetectWorker.h"

#define SESSION_LOCK() \
        if (WorkerSessionLock) \
            sal_mutex_take(WorkerSessionLock, sal_mutex_FOREVER)

#define SESSION_UNLOCK() \
        if (WorkerSessionLock) \
            sal_mutex_give(WorkerSessionLock)


//  Ĭ��1s��Ӧ����, Ӱ��CPUռ����.
#define HANDELER_DEFAULT_INTERVAL (1000000)

// ̽�ⱨ��������ת������
void  PacketHtoN(PacketInfo_S * pstSendMsg)
{
    pstSendMsg->uiSequenceNumber   = htonl(pstSendMsg->uiSequenceNumber);
    pstSendMsg->uiRole   = htonl(pstSendMsg->uiRole);
    pstSendMsg->stT1.uiSec         = htonl(pstSendMsg->stT1.uiSec);
    pstSendMsg->stT1.uiUsec        = htonl(pstSendMsg->stT1.uiUsec);
    pstSendMsg->stT2.uiSec         = htonl(pstSendMsg->stT2.uiSec);
    pstSendMsg->stT2.uiUsec        = htonl(pstSendMsg->stT2.uiUsec);
    pstSendMsg->stT3.uiSec         = htonl(pstSendMsg->stT3.uiSec);
    pstSendMsg->stT3.uiUsec        = htonl(pstSendMsg->stT3.uiUsec);
    pstSendMsg->stT4.uiSec         = htonl(pstSendMsg->stT4.uiSec);
    pstSendMsg->stT4.uiUsec        = htonl(pstSendMsg->stT4.uiUsec);
}

// ̽�ⱨ��������ת������
void  PacketNtoH(PacketInfo_S * pstSendMsg)
{
    pstSendMsg->uiSequenceNumber   = ntohl(pstSendMsg->uiSequenceNumber);
    pstSendMsg->uiRole   = ntohl(pstSendMsg->uiRole);
    pstSendMsg->stT1.uiSec         = ntohl(pstSendMsg->stT1.uiSec);
    pstSendMsg->stT1.uiUsec        = ntohl(pstSendMsg->stT1.uiUsec);
    pstSendMsg->stT2.uiSec         = ntohl(pstSendMsg->stT2.uiSec);
    pstSendMsg->stT2.uiUsec        = ntohl(pstSendMsg->stT2.uiUsec);
    pstSendMsg->stT3.uiSec         = ntohl(pstSendMsg->stT3.uiSec);
    pstSendMsg->stT3.uiUsec        = ntohl(pstSendMsg->stT3.uiUsec);
    pstSendMsg->stT4.uiSec         = ntohl(pstSendMsg->stT4.uiSec);
    pstSendMsg->stT4.uiUsec        = ntohl(pstSendMsg->stT4.uiUsec);
}

// ���캯��, ���г�Ա��ʼ��Ĭ��ֵ.
DetectWorker_C::DetectWorker_C()
{
    struct timespec ts;

    // DETECT_WORKER_INFO("Create a New Worker");

    sal_memset(&stCfg, 0, sizeof(stCfg));
    stCfg.eProtocol = AGENT_DETECT_PROTOCOL_NULL;
    stCfg.uiRole = WORKER_ROLE_CLIENT; // Ĭ��Ϊsender

    WorkerSocket = 0;
    iManageSocket = -1;
    pcAgentCfg = NULL;

    clock_gettime(CLOCK_REALTIME, &ts);
    srandom(ts.tv_nsec + ts.tv_sec); //��ʱ�������������
    // ���������ֵ����0 - RAND_MAX
    uiSequenceNumber = random() % ((UINT32)(-1));

    uiHandlerDefaultInterval = HANDELER_DEFAULT_INTERVAL; //Ĭ��1s��Ӧ����, ����CPUռ����.

    SessionList.clear();

    WorkerSessionLock = NULL;

}

// ��������,�ͷ���Դ
DetectWorker_C::~DetectWorker_C()
{
    DETECT_WORKER_INFO("Destroy Old Worker,uiProtocol[%d], uiSrcIP[%s], uiSrcPort[%d], uiRole[%d]",
                       stCfg.eProtocol, sal_inet_ntoa(stCfg.uiSrcIP), stCfg.uiSrcPort, stCfg.uiRole);

    SESSION_LOCK();
    SessionList.clear(); //��ջỰ����.
    SESSION_UNLOCK();

    // ֹͣ����
    StopThread();

    // �ͷ�socket.
    ReleaseSocket();

    // �ͷŻ�����.
    if(WorkerSessionLock)
        sal_mutex_destroy(WorkerSessionLock);
    WorkerSessionLock = NULL;

}

INT32 DetectWorker_C::RecvServerMsg()
{
    INT32             iSockFd = 0;    // ������ʹ�õ�socket������
    iSockFd = GetManageSocket();
    INT32 iTos = 1;
    INT32 iRet = 0;
    struct timeval tm;      // ���浱ǰʱ��.
    char acCmsgBuf[CMSG_SPACE(sizeof(INT32))];// ���汨�����и�����Ϣ��buffer, ��ǰֻԤ����tosֵ�ռ�.
    struct msghdr msg;      // ����������Ϣ, socket�շ���ʹ��.
    struct cmsghdr *cmsg;   // ���ڱ��� msg.msg_control�����б��ĸ�����Ϣ, Ŀǰ��tosֵ.
    struct iovec iov[1];    // ���ڱ��汨��payload buffer�Ľṹ��.�μ�msg.msg_iov. ��ǰֻʹ��һ��������.
    UINT32 uiMsgType = 0;;

    sal_memset(&tm, 0, sizeof(tm));
    tm.tv_sec  = GetCurrentInterval() / SECOND_USEC;  //us -> s
    tm.tv_usec = GetCurrentInterval() % SECOND_USEC; // us -> us
    iRet = setsockopt(iManageSocket, SOL_SOCKET, SO_RCVTIMEO, &tm, sizeof(tm)); //����socket ��ȡ��ʱʱ��
    if( 0 > iRet )
    {
        DETECT_WORKER_ERROR("RX: Setsockopt SO_RCVTIMEO failed[%d]: %s [%d]", iRet, strerror(errno), errno);
        return AGENT_E_HANDLER;
    }

    // ����payload����buffer
    iov[0].iov_base =  &uiMsgType;
    iov[0].iov_len  = sizeof(UINT32);
    msg.msg_iov = iov;
    msg.msg_iovlen = 1;
    msg.msg_flags = 0;
    msg.msg_control = acCmsgBuf;
    msg.msg_controllen = sizeof(acCmsgBuf);

    DETECT_WORKER_INFO("begin: RecvServerMsg----------------- ");

    // ���ձ���
    iRet = recvmsg(iManageSocket, &msg, 0);
    DETECT_WORKER_INFO("end: RecvServerMsg----------------- ");

    if (iRet == sizeof(UINT32))
    {
        DETECT_WORKER_INFO("RX: RecvServerMsg-----------------  type is:[%d]", uiMsgType);

        switch(uiMsgType)
        {
        case ServerAntsAgentActionStart:
            PROBE_INTERVAL = 60;
            DETECT_WORKER_INFO("Set probe_interval to [%u]. ", PROBE_INTERVAL);
            break;
        case ServerAntsAgentIp:
            SHOULD_REPORT_IP = 1;
            DETECT_WORKER_INFO("Set SHOULD_REPORT_IP to [%u], will report agent ip in next interval. ", SHOULD_REPORT_IP);
            break;
        case ServerAntsAgentConf:
            SHOULD_QUERY_CONF = 1;
            DETECT_WORKER_INFO("Set SHOULD_QUERY_CONF to [%u], will query config in next interval. ", SHOULD_QUERY_CONF);
            break;
        case ServerAntsAgentPingList:
            SHOULD_PROBE = 1;
            DETECT_WORKER_INFO("Set SHOULD_PROBE to [%u], will query pinglist in next interval. ", SHOULD_PROBE);
            break;
        case ServerAntsAgentActionStop:
            PROBE_INTERVAL = 0;
            DETECT_WORKER_INFO("Set probe_interval to [%u], will stop detect. ", PROBE_INTERVAL);
        default:
            DETECT_WORKER_ERROR("Wrong type [%u] ", uiMsgType);
            break;
        }
    }

    msg.msg_control = NULL;
    msg.msg_controllen = 0;

    // IP_TOS����stream(TCP)socket�����޸�ECN bit, ��������»Ḳ��ipͷ������tos�ֶ�
    iRet = setsockopt(iSockFd, SOL_IP, IP_TOS, &iTos, sizeof(iTos));
    if( 0 > iRet)
    {
        DETECT_WORKER_WARNING("RX: Setsockopt IP_TOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
    }

    iRet = sendmsg(iSockFd, &msg, 0);
    if (iRet != sizeof(UINT32) ) // send failed
    {
        DETECT_WORKER_WARNING("RX: Send reply packet failed[%d]: %s [%d]", iRet, strerror(errno), errno);
    }
    return AGENT_OK;
}


// Thread�ص�����.
// PreStopHandler()ִ�к�, ThreadHandler()��Ҫ��GetCurrentInterval() us�������˳�.
INT32 DetectWorker_C::ThreadHandler()
{
    INT32             iSockFd = 0;    // ������ʹ�õ�socket������

    INT32 iTos = 1;   // ������յı��ĵ�tosֵ. ��ʼд��1����Ϊ����Ҫ����socket�ش�tos��Ϣ
    INT32 iRet = 0;

    struct timeval tm;      // ���浱ǰʱ��.
    struct sockaddr_in stPrtnerAddr;    // �Զ�socket��ַ��Ϣ
    char acCmsgBuf[CMSG_SPACE(sizeof(INT32))];// ���汨�����и�����Ϣ��buffer, ��ǰֻԤ����tosֵ�ռ�.
    PacketInfo_S stSendMsg;    // ���汨��payload��Ϣ��buffer, ��ǰֻ����һ������.
    char aucBuffer[1000];
    struct msghdr msg;      // ����������Ϣ, socket�շ���ʹ��.
    struct cmsghdr *cmsg;   // ���ڱ��� msg.msg_control�����б��ĸ�����Ϣ, Ŀǰ��tosֵ.
    struct iovec iov[1];    // ���ڱ��汨��payload buffer�Ľṹ��.�μ�msg.msg_iov. ��ǰֻʹ��һ��������.
    PacketInfo_S *pstSendMsg;

    // �������socket�Ƿ��Ѿ���ʼ���ɹ�.
    while ((!GetSocket()) && GetCurrentInterval())
    {
        sal_usleep(GetCurrentInterval()); //����һ��������ټ��
    }

    if(GetCurrentInterval())
    {
        /*  socket�Ѿ�ready, ��ʱsocket��Protocol�ȳ�ԱӦ���Ѿ���ɳ�ʼ��. */
        iSockFd = GetSocket();

        sal_memset(&tm, 0, sizeof(tm));
        tm.tv_sec  = GetCurrentInterval() / SECOND_USEC;  //us -> s
        tm.tv_usec = GetCurrentInterval() % SECOND_USEC; // us -> us
        iRet = setsockopt(iSockFd, SOL_SOCKET, SO_RCVTIMEO, &tm, sizeof(tm)); //����socket ��ȡ��ʱʱ��
        if( 0 > iRet )
        {
            DETECT_WORKER_ERROR("RX: Setsockopt SO_RCVTIMEO failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            return AGENT_E_HANDLER;
        }

        // ��� msg
        sal_memset(&stSendMsg, 0, sizeof(PacketInfo_S));
        sal_memset(aucBuffer, 0, sizeof(aucBuffer));
        pstSendMsg = (PacketInfo_S*)aucBuffer;

        // �Զ�socket��ַ
        msg.msg_name = &stPrtnerAddr;
        msg.msg_namelen = sizeof(stPrtnerAddr);

        // ����payload����buffer
        iov[0].iov_base =  aucBuffer;
        iov[0].iov_len  = sizeof(PacketInfo_S);
        msg.msg_iov = iov;
        msg.msg_iovlen = 1;

        // ���ĸ�����Ϣbuffer
        msg.msg_control = acCmsgBuf;
        msg.msg_controllen = sizeof(acCmsgBuf);

        // ���flag
        msg.msg_flags = 0;

        // ֪ͨsocket���ձ���ʱ�ش�����tos��Ϣ.
        iRet = setsockopt(iSockFd, SOL_IP, IP_RECVTOS, &iTos, sizeof(iTos));
        if( 0 > iRet )
        {
            DETECT_WORKER_ERROR("RX: Setsockopt IP_RECVTOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            return AGENT_E_HANDLER;
        }

        DETECT_WORKER_INFO("RX: Start Working, Using socket[%u], Protocol[%u], Port[%u], Interval[%dus], Role[%d]",
                           iSockFd, stCfg.eProtocol,stCfg.uiSrcPort, GetCurrentInterval(), stCfg.uiRole);

        while (GetCurrentInterval())
        {
            switch (stCfg.eProtocol)
            {
            case AGENT_DETECT_PROTOCOL_UDP:
                // ��նԶ˵�ַ, payload buffer.
                sal_memset(&stPrtnerAddr, 0, sizeof(stPrtnerAddr));
                sal_memset(&stSendMsg, 0, sizeof(PacketInfo_S));
                sal_memset(acCmsgBuf, 0, sizeof(acCmsgBuf));
                iTos = 0;

                /*
                   �ϰ汾��Linux kernel, sendmsgʱ��֧���趨tos, recvmsg֧�ֻ�ȡtos.
                   Ϊ�˼����ϰ汾, sendmsgʱȥ��msg_control��Ϣ, recvmsgʱ���msg_control��Ϣ.
                */
                // ���ĸ�����Ϣbuffer
                msg.msg_control = acCmsgBuf;
                msg.msg_controllen = sizeof(acCmsgBuf);

                // ���ձ���
                iRet = recvmsg(iSockFd, &msg, 0);
                if (iRet == sizeof(PacketInfo_S) || iRet == sizeof(aucBuffer))
                {
                    sal_memset(&tm, 0, sizeof(tm));
                    gettimeofday(&tm,NULL); //��ȡ��ǰʱ��

                    // ��ȡ�����и�����tos��Ϣ.
                    cmsg = CMSG_FIRSTHDR(&msg);
                    if (cmsg == NULL)
                    {
                        DETECT_WORKER_WARNING("RX: Socket can not get cmsg\n");
                        continue;
                    }
                    if ((cmsg->cmsg_level != SOL_IP) ||
                            (cmsg->cmsg_type != IP_TOS))
                    {
                        DETECT_WORKER_WARNING("RX: Cmsg is not IP_TOS, cmsg_level[%d], cmsg_type[%d]",
                                              cmsg->cmsg_level, cmsg->cmsg_type);
                        continue;
                    }
                    iTos = ((INT32 *) CMSG_DATA(cmsg))[0];


                    PacketNtoH(pstSendMsg);

                    if(WORKER_ROLE_SERVER == pstSendMsg->uiRole)
                    {
                        DETECT_WORKER_INFO("RX: Get reply packet from socket[%d], Len[%d], TOS[%d]",
                                           iSockFd, iRet, iTos);


                        pstSendMsg->stT4.uiSec = tm.tv_sec;
                        pstSendMsg->stT4.uiUsec = tm.tv_usec;
                        iRet = RxUpdateSession(pstSendMsg); //ˢ��sender�ĻỰ�б�

                        // ��Ӧ���ķ��ص�̫��(Timeout), Sender�Ự�б��Ѿ�ɾ���Ự, �᷵���Ҳ���.
                        if ((AGENT_OK!= iRet) && (AGENT_E_NOT_FOUND != iRet))
                            DETECT_WORKER_WARNING("RX: Update Session failed. iRet:[%d]", iRet);
                    }
                    else if(WORKER_ROLE_CLIENT == pstSendMsg->uiRole)
                    {
                        /*
                           �ϰ汾��Linux kernel, sendmsgʱ��֧���趨tos, recvmsg֧�ֻ�ȡtos.
                           Ϊ�˼����ϰ汾, sendmsgʱȥ��msg_control��Ϣ, recvmsgʱ���msg_control��Ϣ.
                        */
                        msg.msg_control = NULL;
                        msg.msg_controllen = 0;

                        // IP_TOS����stream(TCP)socket�����޸�ECN bit, ��������»Ḳ��ipͷ������tos�ֶ�
                        iRet = setsockopt(iSockFd, SOL_IP, IP_TOS, &iTos, sizeof(iTos));
                        if( 0 > iRet)
                        {
                            DETECT_WORKER_WARNING("RX: Setsockopt IP_TOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                            continue;
                        }

                        // ��ӡ��־��ռ�ýϴ�ʱ��.
                        /*
                        DETECT_WORKER_INFO("RX: Send reply packet through socket[%d], Len[%d], TOS[%d], SequenceNumber[%u].",
                                iSockFd, iRet, iTos, pPacketBuffer->uiSequenceNumber);
                        */
                        sal_memset(&tm, 0, sizeof(tm));
                        gettimeofday(&tm,NULL); //��ȡ��ǰʱ��


                        pstSendMsg->stT2.uiSec = tm.tv_sec;
                        pstSendMsg->stT2.uiUsec = tm.tv_usec;
                        pstSendMsg->stT3.uiSec = tm.tv_sec;
                        pstSendMsg->stT3.uiUsec = tm.tv_usec;
                        pstSendMsg->uiRole = WORKER_ROLE_SERVER;
                        PacketHtoN(pstSendMsg); // ����payload������ת������


                        iRet = sendmsg(iSockFd, &msg, 0);
                        if (iRet != sizeof(PacketInfo_S) && iRet != sizeof(aucBuffer)) // send failed
                        {
                            DETECT_WORKER_WARNING("RX: Send reply packet failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                        }
                        sleep(0);
                    }
                }
                else
                {
                    //cout<<"recvfrom timeout" << endl;
                }
                break;

            default :   //��֧�ֵ�Э������, ֱ���˳�
                DETECT_WORKER_ERROR("RX: Unsupported Protocol[%d]", stCfg.eProtocol);
                return AGENT_E_HANDLER;
                break;
            }
            RecvServerMsg();

        }
    }

    DETECT_WORKER_INFO("RX: Task Exiting, Socket[%d], RxInterval[%d]", GetSocket(), GetCurrentInterval());
    return AGENT_OK;
}

// Thread��������, ֪ͨThreadHandler����׼��.
INT32 DetectWorker_C::PreStartHandler()
{

    SetNewInterval(uiHandlerDefaultInterval);
    return AGENT_OK;
}

// Thread����ֹͣ, ֪ͨThreadHandler�����˳�.
INT32 DetectWorker_C::PreStopHandler()
{
    SetNewInterval(0);
    return AGENT_OK;
}


INT32 DetectWorker_C::InitCfg(WorkerCfg_S stNewWorker)
{
    switch (stNewWorker.eProtocol)
    {
    case AGENT_DETECT_PROTOCOL_UDP:
        if (0 == stNewWorker.uiSrcPort) // socketҪ��Դ�˿�, �˿ںŲ���Ϊ0.
        {
            DETECT_WORKER_ERROR("SrcPort is 0");
            return AGENT_E_PARA;
        }
        if (INADDR_NONE == stNewWorker.uiSrcIP) // socketҪ��Դ�˿�, �˿ںŲ���Ϊ0.
        {
            DETECT_WORKER_ERROR("Invalid SrcIP");
            return AGENT_E_PARA;
        }

        stCfg.uiSrcIP = stNewWorker.uiSrcIP;
        stCfg.uiSrcPort    = stNewWorker.uiSrcPort;

        /*
        DETECT_WORKER_INFO("Init New Worker,uiProtocol[%d], uiSrcIP[%s], uiSrcPort[%d], uiRole[%d]",
            stNewWorker.eProtocol, sal_inet_ntoa(stNewWorker.uiSrcIP), stNewWorker.uiSrcPort, stNewWorker.uiRole);
        */
        break;

    case AGENT_DETECT_PROTOCOL_ICMP:
    case AGENT_DETECT_PROTOCOL_TCP:
    default:
        DETECT_WORKER_ERROR("Unsupported Protocol[%d]",stNewWorker.eProtocol);
        return AGENT_E_PARA;
    }

    stCfg.eProtocol   = stNewWorker.eProtocol;
    stCfg.uiRole    = stNewWorker.uiRole;
    return AGENT_OK;
}

INT32 DetectWorker_C::Init(WorkerCfg_S stNewWorker, ServerAntAgentCfg_C *pcNewAgentCfg)
{
    INT32 iRet = AGENT_OK;

    if (WorkerSessionLock)  //��֧���ظ���ʼ��Worker, ��cfg���Ᵽ��.
    {
        DETECT_WORKER_ERROR("Do not reinit this worker");
        return AGENT_E_ERROR;
    }

    pcAgentCfg = pcNewAgentCfg;

    // ����worker��ɫ��ͬ, ��ʼ��stCfg, ͬʱ������μ��
    switch (stNewWorker.uiRole)
    {
    case WORKER_ROLE_CLIENT:  //��ʱ�������ֽ�ɫ,
    case WORKER_ROLE_SERVER:
        iRet = InitCfg(stNewWorker);
        if(iRet)
        {
            DETECT_WORKER_ERROR("Init worker cfg failed");
            return iRet;
        }
        break;
    default:
        DETECT_WORKER_ERROR("Unsupported Role[%d]",stNewWorker.uiRole);
        return AGENT_E_PARA;
    }

    // ���뻥������Դ
    WorkerSessionLock = sal_mutex_create("DetectWorker_SESSION");
    if( NULL == WorkerSessionLock )
    {
        DETECT_WORKER_ERROR("Create mutex failed");
        return AGENT_E_MEMORY;
    }


    StopThread(); // �޸�socket֮ǰ,����ֹͣrx����

    iRet = InitSocket(); // ��ʼ��socket
    if (iRet && (AGENT_E_SOCKET != iRet)) // ��socket����ʱ���˳�.
    {
        DETECT_WORKER_ERROR("InitSocket failed[%d]", iRet);
        return iRet;
    }

    iRet = InitManageSocket();
    if (iRet && (AGENT_E_SOCKET != iRet)) // ��socket����ʱ���˳�.
    {
        DETECT_WORKER_ERROR("InitSocket failed[%d]", iRet);
        return iRet;
    }

    iRet = StartThread(); // ����rx����
    if(iRet)
    {
        DETECT_WORKER_ERROR("StartRxThread failed[%d]", iRet);
        return iRet;
    }
    return iRet;
}

// �ͷ�socket��Դ
INT32 DetectWorker_C::ReleaseSocket()
{

    if(WorkerSocket)
    {
        close(WorkerSocket);
        WorkerSocket = 0;
    }

    return AGENT_OK;
}

INT32 DetectWorker_C::ReleaseManageSocket()
{

    if(-1 != iManageSocket)
    {
        close(iManageSocket);
        iManageSocket = 0;
    }

    return AGENT_OK;
}

// ����stCfg��Ϣ����socket��Դ.
INT32 DetectWorker_C::InitSocket()
{
    INT32 SocketTmp = 0;
    struct sockaddr_in servaddr;
    INT32 iRet;
    UINT32 uiSrcPortMin = 0, uiSrcPortMax=0, uiDestPort=0;


    iRet = pcAgentCfg ->GetProtocolUDP(&uiSrcPortMin, &uiSrcPortMax, &uiDestPort);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Get Protocol UDP cfg failed[%d]", iRet);
        return AGENT_E_PARA;
    }
    FLOW_MANAGER_INFO("InitSocket~~~~~~~~~~~~~~~~~~~~~~~~[%d]", uiDestPort);
    ReleaseSocket();

    // ����Э������, ������Ӧsocket.
    switch (stCfg.eProtocol)
    {
    case AGENT_DETECT_PROTOCOL_UDP:
        SocketTmp = socket(AF_INET, SOCK_DGRAM, 0);
        if( SocketTmp == -1 )
        {
            DETECT_WORKER_ERROR("Create socket failed[%d]: %s [%d]", SocketTmp, strerror(errno), errno);
            return AGENT_E_MEMORY;
        }
        sal_memset(&servaddr, 0, sizeof(servaddr));
        servaddr.sin_family = AF_INET;
        //servaddr.sin_addr.s_addr = htonl(stCfg.uiSrcIP);
        servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
        servaddr.sin_port = htons(uiDestPort);

        if( bind(SocketTmp, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1)
        {
            DETECT_WORKER_WARNING("Bind socket failed, SrcIP[%s],SrcPort[%d]: %s [%d]",
                                  sal_inet_ntoa(stCfg.uiSrcIP), stCfg.uiSrcPort, strerror(errno), errno);
            close(SocketTmp);
            return AGENT_E_SOCKET;
        }
        break;

    case AGENT_DETECT_PROTOCOL_ICMP:
    case AGENT_DETECT_PROTOCOL_TCP:
    default:
        DETECT_WORKER_ERROR("Unsupported Protocol[%d]",stCfg.eProtocol);
        return AGENT_E_PARA;
    }

    WorkerSocket = SocketTmp;

    DETECT_WORKER_INFO("Init a new socket [%d], Bind: %d,IP,%u", WorkerSocket,uiDestPort,servaddr.sin_addr.s_addr);

    return AGENT_OK;
}


// ����stCfg��Ϣ����socket��Դ.
INT32 DetectWorker_C::InitManageSocket()
{
    INT32 SocketTmp = 0;
    struct sockaddr_in servaddr;
    INT32 iRet;
    UINT32 uiMgntIp = 0;
    UINT32 uiSrcPortMin = 0;

    pcAgentCfg ->GetMgntIP(&uiMgntIp);
    if (0 == uiMgntIp)
    {
        DETECT_WORKER_ERROR("GetMgntIP failed ");

        return AGENT_E_SOCKET;
    }

    ReleaseManageSocket();

    SocketTmp = socket(AF_INET, SOCK_DGRAM, 0);
    if( SocketTmp == -1 )
    {
        DETECT_WORKER_ERROR("Create socket failed[%d]: %s [%d]", SocketTmp, strerror(errno), errno);
        return AGENT_E_MEMORY;
    }
    sal_memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(uiMgntIp);
    servaddr.sin_port = htons(33001);

    if( bind(SocketTmp, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1)
    {
        DETECT_WORKER_WARNING("Bind socket failed, SrcIP[%s],SrcPort[%d]: %s [%d]",
                              sal_inet_ntoa(stCfg.uiSrcIP), stCfg.uiSrcPort, strerror(errno), errno);
        close(SocketTmp);
        return AGENT_E_SOCKET;
    }

    iManageSocket = SocketTmp;

    DETECT_WORKER_INFO("Init a new socket [%d], Bind: %d,IP,%u", iManageSocket,33001,servaddr.sin_addr.s_addr);

    return AGENT_OK;
}


// ��ȡ��ǰsocket, ����������
INT32 DetectWorker_C::GetSocket()
{
    INT32 SocketTmp;

    SocketTmp = WorkerSocket;

    return SocketTmp;
}

INT32 DetectWorker_C::GetManageSocket()
{
    INT32 SocketTmp;

    SocketTmp = iManageSocket;

    return SocketTmp;
}


// Rx�����յ�Ӧ���ĺ�, ֪ͨworkerˢ�»Ự�б�, sender��Rx����ʹ��.
INT32 DetectWorker_C::RxUpdateSession
(PacketInfo_S * pstPakcet)
{
    INT32 iRet = AGENT_E_NOT_FOUND;

    SESSION_LOCK();
    vector<DetectWorkerSession_S>::iterator pSession;
    for(pSession = SessionList.begin(); pSession != SessionList.end(); pSession++)
    {
        if ( ( pstPakcet->uiSequenceNumber == pSession ->uiSequenceNumber )
                && (SESSION_STATE_WAITING_REPLY == pSession->uiSessionState) )
        {
            DETECT_WORKER_INFO("RX: RxUpdateSession st2[%u], st3[%u]",
                               pstPakcet->stT2.uiSec, pstPakcet->stT3.uiSec);
            pSession->stT2 = pstPakcet->stT2;
            pSession->stT3 = pstPakcet->stT3;
            pSession->stT4 = pstPakcet->stT4;
            pSession->uiSessionState = SESSION_STATE_WAITING_CHECK;
            iRet = AGENT_OK;
        }
    }
    SESSION_UNLOCK();

    return iRet;
}

// TX���ͱ��Ľ�����ˢ�»Ự״̬
INT32 DetectWorker_C::TxUpdateSession
(DetectWorkerSession_S* pNewSession)
{
    INT32 iRet = AGENT_E_NOT_FOUND;

    SESSION_LOCK();
    vector<DetectWorkerSession_S>::iterator pSession;
    for(pSession = SessionList.begin(); pSession != SessionList.end(); pSession++)
    {
        if ( ( pNewSession->uiSequenceNumber == pSession ->uiSequenceNumber )
                && (SESSION_STATE_INITED == pSession->uiSessionState) )
        {
            *pSession = *pNewSession;
            iRet = AGENT_OK;
        }
    }
    SESSION_UNLOCK();

    return iRet;
}

// �������ķ���.PushSession()ʱ����.
INT32 DetectWorker_C::TxPacket(DetectWorkerSession_S*
                               pNewSession)
{
    INT32 iRet = AGENT_OK;
    struct timeval tm;
    PacketInfo_S stSendMsg;
    PacketInfo_S *pstSendMsg;
    char aucBuff[1000];
    struct sockaddr_in servaddr;
    INT32 tos = 0;

    sal_memset(&servaddr, 0, sizeof(servaddr));
    sal_memset(&tm, 0, sizeof(tm));

    pstSendMsg = (PacketInfo_S *)aucBuff;
    gettimeofday(&tm,NULL); //��ȡ��ǰʱ��
    if (pNewSession->stFlowKey.uiIsBigPkg)
    {
        sal_memset(aucBuff, 0, sizeof(aucBuff));
        pstSendMsg->uiSequenceNumber = pNewSession->uiSequenceNumber;
        pstSendMsg->stT1.uiSec = tm.tv_sec;
        pstSendMsg->stT1.uiUsec = tm.tv_usec;
        pstSendMsg->uiRole = WORKER_ROLE_CLIENT;
        pNewSession->stT1 = pstSendMsg->stT1; //����T1ʱ��
    }
    else
    {
        sal_memset(&stSendMsg, 0, sizeof(PacketInfo_S));
        stSendMsg.uiSequenceNumber = pNewSession->uiSequenceNumber;
        stSendMsg.stT1.uiSec = tm.tv_sec;
        stSendMsg.stT1.uiUsec = tm.tv_usec;
        stSendMsg.uiRole = WORKER_ROLE_CLIENT;
        pNewSession->stT1 = stSendMsg.stT1; //����T1ʱ��
    }

    // ���socket�Ƿ��Ѿ�ready
    if( 0 == GetSocket())
    {
        iRet = InitSocket(); //�������°�socket
        if(iRet)
        {
            DETECT_WORKER_WARNING("Init Socket failed again[%d]", iRet);
            return iRet;
        }
    }

    switch (pNewSession->stFlowKey.eProtocol)
    {
    case AGENT_DETECT_PROTOCOL_UDP:
        servaddr.sin_family = AF_INET;
        servaddr.sin_addr.s_addr = htonl(pNewSession->stFlowKey.uiDestIP);
        servaddr.sin_port = htons(pNewSession->stFlowKey.uiDestPort);
        DETECT_WORKER_INFO("dscp is [%u]", pcAgentCfg->getDscp());
        if (0 != pcAgentCfg->getDscp())
        {
            tos = (pcAgentCfg->getDscp())<<2; //dscp����2λ, ���tos
        }
        else
        {
            tos = (pNewSession->stFlowKey.uiDscp)<<2; //dscp����2λ, ���tos
        }
        // IP_TOS����stream(TCP)socket�����޸�ECN bit, ��������»Ḳ��ipͷ������tos�ֶ�
        iRet = setsockopt(GetSocket(), SOL_IP, IP_TOS, &tos, sizeof(tos));
        if( 0 > iRet )
        {
            DETECT_WORKER_ERROR("TX: Setsockopt IP_TOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            iRet = AGENT_E_PARA;
            break;
        }

        if (pNewSession->stFlowKey.uiIsBigPkg)
        {
            PacketHtoN(pstSendMsg);// ������ת������
            iRet = sendto(GetSocket(), aucBuff, sizeof(aucBuff), 0, (sockaddr *)&servaddr, sizeof(servaddr));
            DETECT_WORKER_INFO("set big package size [%d], iRet is [%d]", sizeof(PacketInfo_S), iRet);
        }
        else
        {
            PacketHtoN(&stSendMsg);// ������ת������
            iRet = sendto(GetSocket(), &stSendMsg, sizeof(PacketInfo_S), 0, (sockaddr *)&servaddr, sizeof(servaddr));
            DETECT_WORKER_INFO("set package size [%d], iRet is [%d]", sizeof(PacketInfo_S), iRet);
        }


        if (sizeof(PacketInfo_S) == iRet || iRet == sizeof(aucBuff)) //���ͳɹ�.
        {
            pNewSession->uiSessionState = SESSION_STATE_WAITING_REPLY;
            iRet = TxUpdateSession(pNewSession);
            if( iRet )
            {
                DETECT_WORKER_WARNING("TX: Tx Update Session[%d]", iRet);
            }
        }
        else //����ʧ��
        {
            DETECT_WORKER_ERROR("TX: Send Detect Packet failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            iRet = AGENT_E_ERROR;
        }
        break;

    default:
        DETECT_WORKER_ERROR("Unsupported Protocol[%d]", pNewSession->stFlowKey.eProtocol);
        iRet = AGENT_E_PARA;
    }
    return iRet;
}

// ���̽������, FlowManageʹ��.
INT32 DetectWorker_C::PushSession(FlowKey_S stNewFlow)
{
    INT32 iRet = AGENT_OK;

    DetectWorkerSession_S stNewSession;
    sal_memset(&stNewSession, 0, sizeof(stNewSession));

    // ��μ��,��������.
    if (WORKER_ROLE_SERVER == stCfg.uiRole)     // Target�˲�����ѹ��̽��Ự
    {
        DETECT_WORKER_ERROR("Role target do not support POP session");
        return AGENT_E_PARA;
    }

    if (stNewFlow.eProtocol != stCfg.eProtocol)  // ���flow��Э���Ƿ��뵱ǰworkerƥ��
    {
        DETECT_WORKER_ERROR("New session Protocol do not match this worker");
        return AGENT_E_PARA;
    }

    if ( SAL_INADDR_ANY !=  stCfg.uiSrcIP
            && (stNewFlow.uiSrcIP!= stCfg.uiSrcIP))    // ���flow��ԴIP�Ƿ��뵱ǰworkerƥ��. stProtocol.uiSrcIPΪ0��ʾƥ������IP.
    {
        DETECT_WORKER_ERROR("New session SrcIP do not match this worker. New Session IP:[%s]",
                            sal_inet_ntoa(stNewFlow.uiSrcIP));

        DETECT_WORKER_ERROR("But this worker IP:[%s]", sal_inet_ntoa(stCfg.uiSrcIP));
        return AGENT_E_PARA;
    }

    //  ������ʱ���У���ͻ������ɾ��
    if (stNewFlow.uiDscp > AGENT_MAX_DSCP_VALUE)  // ���flow��dscp�Ƿ�Ϸ�
    {
        DETECT_WORKER_ERROR("New session dscp[%d] is bigger than the max value[%d]", stNewFlow.uiDscp, AGENT_MAX_DSCP_VALUE);
        return AGENT_E_PARA;
    }


    // ��μ��,����Э���������ּ��.
    switch (stNewFlow.eProtocol)
    {
    case AGENT_DETECT_PROTOCOL_UDP:
        break;

    default:
        DETECT_WORKER_ERROR("Unsupported Protocol[%d]", stNewFlow.eProtocol);
        return AGENT_E_PARA;
    }

    // ���ͨ��.
    stNewSession.stFlowKey = stNewFlow;
    stNewSession.uiSequenceNumber = uiSequenceNumber++; // ��ȡ���к�
    stNewSession.uiSessionState = SESSION_STATE_INITED; // ��ʼ��״̬��.


    // ѹ��Ự�б�, rx�����յ�Ӧ���ĺ��������кŲ��һỰ�б�.
    SESSION_LOCK();
    SessionList.push_back(stNewSession);
    SESSION_UNLOCK();

    // ����̽�ⱨ�ķ���
    iRet = TxPacket(&stNewSession);
    if (iRet)
    {
        DETECT_WORKER_WARNING("TX: TxPacket failed[%d]", iRet);

        stNewSession.uiSessionState = SESSION_STATE_SEND_FAIELD;
        iRet = TxUpdateSession(&stNewSession); //ˢ��״̬��.
        if( iRet )
        {
            DETECT_WORKER_WARNING("TX: Tx Update Session[%d]", iRet);
        }
        return iRet;
    }

    return iRet;
}

// ��ѯ̽����, FlowManageʹ��.
INT32 DetectWorker_C::PopSession(DetectWorkerSession_S* pOldSession)
{
    INT32 iRet = AGENT_E_NOT_FOUND;


    SESSION_LOCK();
    vector<DetectWorkerSession_S>::iterator pSession;
    for(pSession = SessionList.begin(); pSession != SessionList.end(); pSession++)
    {
        if ( (SESSION_STATE_SEND_FAIELD == pSession->uiSessionState )
                ||(SESSION_STATE_WAITING_REPLY == pSession->uiSessionState )
                ||(SESSION_STATE_WAITING_CHECK == pSession->uiSessionState ))  // �Ѿ��յ����Ļ������ڵȴ�Ӧ����
        {
            if (SESSION_STATE_WAITING_REPLY == pSession->uiSessionState)  // ��ʱ��δ�յ�Ӧ������ζ�ų�ʱ.
            {
                struct timeval tm;
                pSession->uiSessionState = SESSION_STATE_TIMEOUT;

                sal_memset(&tm, 0, sizeof(tm));
                gettimeofday(&tm,NULL); // ��ȡ��ǰʱ��
                pSession->stT4.uiSec = tm.tv_sec;
                pSession->stT4.uiUsec = tm.tv_usec;
            }

            *pOldSession = *pSession;
            SessionList.erase(pSession);
            iRet = AGENT_OK;
            break;
        }
    }
    SESSION_UNLOCK();

    return iRet;
}

