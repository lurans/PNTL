
#include <stdlib.h>
#include <unistd.h>

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

#define SOCKET_LOCK() \
        if (WorkerSocketLock) \
            sal_mutex_take(WorkerSocketLock, sal_mutex_FOREVER)

#define SOCKET_UNLOCK() \
        if (WorkerSocketLock) \
            sal_mutex_give(WorkerSocketLock)

//  默认1s响应周期, 影响CPU占用率.
#define HANDELER_DEFAULT_INTERVAL (1000000)

// 探测报文主机序转网络序
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

// 探测报文网络序转主机序
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

// 构造函数, 所有成员初始化默认值.
DetectWorker_C::DetectWorker_C()
{
    struct timespec ts;

    // DETECT_WORKER_INFO("Create a New Worker");

    sal_memset(&stCfg, 0, sizeof(stCfg));
    stCfg.eProtocol = AGENT_DETECT_PROTOCOL_NULL;
    stCfg.uiRole = WORKER_ROLE_CLIENT; // 默认为sender

    WorkerSocket = 0;
    pcAgentCfg = NULL;

    clock_gettime(CLOCK_REALTIME, &ts);
    srandom(ts.tv_nsec + ts.tv_sec); //用时间做随机数种子
    // 随机数返回值介于0 - RAND_MAX
    uiSequenceNumber = random() % ((UINT32)(-1));

    uiHandlerDefaultInterval = HANDELER_DEFAULT_INTERVAL; //默认1s响应周期, 降低CPU占用率.

    SessionList.clear();

    WorkerSessionLock = NULL;
    WorkerSocketLock = NULL;
}

// 析构函数,释放资源
DetectWorker_C::~DetectWorker_C()
{
    DETECT_WORKER_INFO("Destroy Old Worker,uiProtocol[%d], uiSrcIP[%s], uiSrcPort[%d], uiRole[%d]",
                stCfg.eProtocol, sal_inet_ntoa(stCfg.uiSrcIP), stCfg.uiSrcPort, stCfg.uiRole);

    SESSION_LOCK();
    SessionList.clear(); //清空会话链表.
    SESSION_UNLOCK();

    // 停止任务
    StopThread();

    // 释放socket.
    ReleaseSocket();

    // 释放互斥锁.
    if(WorkerSessionLock)
        sal_mutex_destroy(WorkerSessionLock);
    WorkerSessionLock = NULL;

    if(WorkerSocketLock)
        sal_mutex_destroy(WorkerSocketLock);
    WorkerSocketLock = NULL;
}

// Thread回调函数.
// PreStopHandler()执行后, ThreadHandler()需要在GetCurrentInterval() us内主动退出.
INT32 DetectWorker_C::ThreadHandler()
{
    INT32             iSockFd = 0;    // 本任务使用的socket描述符

    INT32 iTos = 1;   // 保存接收的报文的tos值. 初始写成1是因为后续要配置socket回传tos信息
    INT32 iRet = 0;

    struct timeval tm;      // ���浱ǰʱ��.
    struct sockaddr_in stPrtnerAddr;    // �Զ�socket��ַ��Ϣ
    char acCmsgBuf[CMSG_SPACE(sizeof(INT32))];// ���汨�����и�����Ϣ��buffer, ��ǰֻԤ����tosֵ�ռ�.
    PacketInfo_S  stSendMsg;    // ���汨��payload��Ϣ��buffer, ��ǰֻ����һ������.

    struct msghdr msg;      // ����������Ϣ, socket�շ���ʹ��.
    struct cmsghdr *cmsg;   // ���ڱ��� msg.msg_control�����б��ĸ�����Ϣ, Ŀǰ��tosֵ.
    struct iovec iov[1];    // ���ڱ��汨��payload buffer�Ľṹ��.�μ�msg.msg_iov. ��ǰֻʹ��һ��������.


    // 检查对象的socket是否已经初始化成功.
    while ((!GetSocket()) && GetCurrentInterval())
    {
        sal_usleep(GetCurrentInterval()); //休眠一个间隔后再检查
    }

    if(GetCurrentInterval())
    {
        /*  socket已经ready, 此时socket和Protocol等成员应该已经完成初始化. */
        iSockFd = GetSocket();

        sal_memset(&tm, 0, sizeof(tm));
        tm.tv_sec  = 3*GetCurrentInterval() / SECOND_USEC;  //us -> s
        tm.tv_usec = GetCurrentInterval() % SECOND_USEC; // us -> us
        iRet = setsockopt(iSockFd, SOL_SOCKET, SO_RCVTIMEO, &tm, sizeof(tm)); //设置socket 读取超时时间
        if( 0 > iRet )
        {
            DETECT_WORKER_ERROR("RX: Setsockopt SO_RCVTIMEO failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            return AGENT_E_HANDLER;
        }


        // ��� msg
         sal_memset(&stSendMsg, 0, sizeof(PacketInfo_S));


        // 对端socket地址
        msg.msg_name = &stPrtnerAddr;
        msg.msg_namelen = sizeof(stPrtnerAddr);

        // ����payload����buffer
        iov[0].iov_base = &stSendMsg;

        iov[0].iov_len  = sizeof(PacketInfo_S);
        msg.msg_iov = iov;
        msg.msg_iovlen = 1;

        // 报文附加信息buffer
        msg.msg_control = acCmsgBuf;
        msg.msg_controllen = sizeof(acCmsgBuf);

        // 清空flag
        msg.msg_flags = 0;

        // 通知socket接收报文时回传报文tos信息.
        iRet = setsockopt(iSockFd, SOL_IP, IP_RECVTOS, &iTos, sizeof(iTos));
        if( 0 > iRet )
        {
            DETECT_WORKER_ERROR("RX: Setsockopt IP_RECVTOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
            return AGENT_E_HANDLER;
        }

        DETECT_WORKER_INFO("RX: @@@@@@@@@@@@@@@@@@@@@@@@@@Start Working, Using socket[%u], Protocol[%u], Port[%u], Interval[%dus], Role[%d]",
                iSockFd, stCfg.eProtocol,stCfg.uiSrcPort, GetCurrentInterval(), stCfg.uiRole);

        while (GetCurrentInterval())
        {
            switch (stCfg.eProtocol)
            {
                case AGENT_DETECT_PROTOCOL_UDP:
                    // 清空对端地址, payload buffer.
                    sal_memset(&stPrtnerAddr, 0, sizeof(stPrtnerAddr));
                    sal_memset(&stSendMsg, 0, sizeof(PacketInfo_S));
                    sal_memset(acCmsgBuf, 0, sizeof(acCmsgBuf));

                    iTos = 0;

                    /*
                       老版本的Linux kernel, sendmsg时不支持设定tos, recvmsg支持获取tos.
                       为了兼容老版本, sendmsg时去除msg_control信息, recvmsg时添加msg_control信息.
                    */
                    // 报文附加信息buffer
                    msg.msg_control = acCmsgBuf;
                    msg.msg_controllen = sizeof(acCmsgBuf);

                    // 接收报文
                    iRet = recvmsg(iSockFd, &msg, 0);
                    if (iRet == sizeof(PacketInfo_S))
                    {
                        sal_memset(&tm, 0, sizeof(tm));
                        gettimeofday(&tm,NULL); //获取当前时间

                        // 获取报文中附带的tos信息.
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

                        PacketNtoH(&stSendMsg); // ����payload������ת������

                        if(WORKER_ROLE_SERVER == stSendMsg.uiRole)
                        {
                            /*
                            DETECT_WORKER_INFO("RX: Get reply packet from socket[%d], Len[%d], TOS[%d]",
                                    iSockFd, iRet, iTos);
                            */

                            stSendMsg.stT4.uiSec = tm.tv_sec;
                            stSendMsg.stT4.uiUsec = tm.tv_usec;
                            iRet = RxUpdateSession(&stSendMsg); //ˢ��sender�ĻỰ�б�
                            // ��Ӧ���ķ��ص�̫��(Timeout), Sender�Ự�б��Ѿ�ɾ���Ự, �᷵���Ҳ���.

                            if ((AGENT_OK!= iRet) && (AGENT_E_NOT_FOUND != iRet))
                                DETECT_WORKER_WARNING("RX: Update Session failed. iRet:[%d]", iRet);
                        }
                        else if(WORKER_ROLE_CLIENT == stSendMsg.uiRole)
                        {
                            stSendMsg.stT2.uiSec = tm.tv_sec;
                            stSendMsg.stT2.uiUsec = tm.tv_usec;

                            /*
                               老版本的Linux kernel, sendmsg时不支持设定tos, recvmsg支持获取tos.
                               为了兼容老版本, sendmsg时去除msg_control信息, recvmsg时添加msg_control信息.
                            */
                            msg.msg_control = NULL;
                            msg.msg_controllen = 0;

                            // IP_TOS对于stream(TCP)socket不会修改ECN bit, 其他情况下会覆盖ip头中整个tos字段
                            iRet = setsockopt(iSockFd, SOL_IP, IP_TOS, &iTos, sizeof(iTos));
                            if( 0 > iRet)
                            {
                                DETECT_WORKER_WARNING("RX: Setsockopt IP_TOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                                continue;
                            }

                            // 打印日志会占用较大时间.
                            /*
                            DETECT_WORKER_INFO("RX: Send reply packet through socket[%d], Len[%d], TOS[%d], SequenceNumber[%u].",
                                    iSockFd, iRet, iTos, pPacketBuffer->uiSequenceNumber);
                            */

                            sal_memset(&tm, 0, sizeof(tm));
                            gettimeofday(&tm,NULL); //��ȡ��ǰʱ��
                            stSendMsg.stT3.uiSec = tm.tv_sec;
                            stSendMsg.stT3.uiUsec = tm.tv_usec;
    			 			stSendMsg.uiRole = WORKER_ROLE_SERVER;

                            PacketHtoN(&stSendMsg); // ����payload������ת������

                            iRet = sendmsg(iSockFd, &msg, 0);
                            if (iRet != sizeof(PacketInfo_S)) // send failed
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

                default :   //不支持的协议类型, 直接退出
                    DETECT_WORKER_ERROR("RX: Unsupported Protocol[%d]", stCfg.eProtocol);
                    return AGENT_E_HANDLER;
                    break;
            }
        }
    }

    DETECT_WORKER_INFO("RX: Task Exiting, Socket[%d], RxInterval[%d]", GetSocket(), GetCurrentInterval());
    return AGENT_OK;
}

// Thread即将启动, 通知ThreadHandler做好准备.
INT32 DetectWorker_C::PreStartHandler()
{

    SetNewInterval(uiHandlerDefaultInterval);
    return AGENT_OK;
}

// Thread即将停止, 通知ThreadHandler主动退出.
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
            if (0 == stNewWorker.uiSrcPort) // socket要绑定源端口, 端口号不能为0.
            {
                DETECT_WORKER_ERROR("SrcPort is 0");
                return AGENT_E_PARA;
            }
            if (INADDR_NONE == stNewWorker.uiSrcIP) // socket要绑定源端口, 端口号不能为0.
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

    if (WorkerSessionLock)  //不支持重复初始化Worker, 简化cfg互斥保护.
    {
        DETECT_WORKER_ERROR("Do not reinit this worker");
        return AGENT_E_ERROR;
    }

    pcAgentCfg = pcNewAgentCfg;

    // 根据worker角色不同, 初始化stCfg, 同时进行入参检查
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

    // 申请互斥锁资源
    WorkerSessionLock = sal_mutex_create("DetectWorker_SESSION");
    if( NULL == WorkerSessionLock )
    {
        DETECT_WORKER_ERROR("Create mutex failed");
        return AGENT_E_MEMORY;
    }

    WorkerSocketLock = sal_mutex_create("DetectWorker_SOCKET");
    if( NULL == WorkerSocketLock )
    {
        DETECT_WORKER_ERROR("Create mutex failed");
        return AGENT_E_MEMORY;
    }


    StopThread(); // 修改socket之前,需先停止rx任务

    iRet = InitSocket(); // 初始化socket
    if (iRet && (AGENT_E_SOCKET != iRet)) // 绑定socket出错时不退出.
    {
        DETECT_WORKER_ERROR("InitSocket failed[%d]", iRet);
        return iRet;
    }

    iRet = StartThread(); // 启动rx任务
    if(iRet)
    {
        DETECT_WORKER_ERROR("StartRxThread failed[%d]", iRet);
        return iRet;
    }
    return iRet;
}

// 释放socket资源
INT32 DetectWorker_C::ReleaseSocket()
{
    if(WorkerSocket)
        DETECT_WORKER_INFO("Release a socket [%d]", WorkerSocket);

    SOCKET_LOCK();
    if(WorkerSocket)
        close(WorkerSocket);
    WorkerSocket = 0;
    SOCKET_UNLOCK();

    return AGENT_OK;
}

// 根据stCfg信息申请socket资源.
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

    // 根据协议类型, 创建对应socket.
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


    SOCKET_LOCK();
    WorkerSocket = SocketTmp;
    SOCKET_UNLOCK();

    DETECT_WORKER_INFO("Init a new socket [%d], Bind: %d,IP,%u", WorkerSocket,uiDestPort,servaddr.sin_addr.s_addr);


    return AGENT_OK;
}


// 获取当前socket, 互斥锁保护
INT32 DetectWorker_C::GetSocket()
{
    INT32 SocketTmp;

    SOCKET_LOCK();
    SocketTmp = WorkerSocket;
    SOCKET_UNLOCK();

    return SocketTmp;
}

// Rx任务收到应答报文后, 通知worker刷新会话列表, sender的Rx任务使用.
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

// TX发送报文结束后刷新会话状态
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

// 启动报文发送.PushSession()时触发.
INT32 DetectWorker_C::TxPacket(DetectWorkerSession_S*
                        pNewSession)
{
    INT32 iRet = AGENT_OK;
    struct timeval tm;
    PacketInfo_S * pstSendMsg = NULL;
    struct sockaddr_in servaddr;
    INT32 tos = 0;

    sal_memset(&servaddr, 0, sizeof(servaddr));

    pstSendMsg = new PacketInfo_S;
    if (NULL == pstSendMsg)
    {
        DETECT_WORKER_ERROR("No enough memory");
        return AGENT_E_MEMORY;
    }
    sal_memset(pstSendMsg, 0, sizeof(PacketInfo_S));

    pstSendMsg->uiSequenceNumber = pNewSession->uiSequenceNumber;
    sal_memset(&tm, 0, sizeof(tm));
    gettimeofday(&tm,NULL); //��ȡ��ǰʱ��


    pstSendMsg->stT1.uiSec = tm.tv_sec;
    pstSendMsg->stT1.uiUsec = tm.tv_usec;
    pstSendMsg->uiRole = WORKER_ROLE_CLIENT;

    pNewSession->stT1 = pstSendMsg->stT1; //����T1ʱ��


    // 检查socket是否已经ready
    if( 0 == GetSocket())
    {
        iRet = InitSocket(); //尝试重新绑定socket
        if(iRet)
        {
            DETECT_WORKER_WARNING("Init Socket failed again[%d]", iRet);
            delete pstSendMsg;
            pstSendMsg = NULL;
            return iRet;
        }
    }

    SOCKET_LOCK(); //同一时间只允许一个任务通过该socket发包.

    switch (pNewSession->stFlowKey.eProtocol)
    {
        case AGENT_DETECT_PROTOCOL_UDP:
            servaddr.sin_family = AF_INET;
            servaddr.sin_addr.s_addr = htonl(pNewSession->stFlowKey.uiDestIP);
            servaddr.sin_port = htons(pNewSession->stFlowKey.uiDestPort);

            tos = (pNewSession->stFlowKey.uiDscp)<<2; //dscp左移2位, 变成tos

            // IP_TOS对于stream(TCP)socket不会修改ECN bit, 其他情况下会覆盖ip头中整个tos字段
            iRet = setsockopt(GetSocket(), SOL_IP, IP_TOS, &tos, sizeof(tos));
            if( 0 > iRet )
            {
                DETECT_WORKER_ERROR("TX: Setsockopt IP_TOS failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                iRet = AGENT_E_PARA;
                break;
            }

            PacketHtoN(pstSendMsg);// ������ת������
            iRet = sendto(GetSocket(), pstSendMsg, sizeof(PacketInfo_S), 0, (sockaddr *)&servaddr, sizeof(servaddr));
            if (sizeof(PacketInfo_S) == iRet) //���ͳɹ�.

            {
                pNewSession->uiSessionState = SESSION_STATE_WAITING_REPLY;

                iRet = TxUpdateSession(pNewSession);
                if( iRet )
                {
                    DETECT_WORKER_WARNING("TX: Tx Update Session[%d]", iRet);
                }
            }
            else //发送失败
            {
                DETECT_WORKER_ERROR("TX: Send Detect Packet failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                iRet = AGENT_E_ERROR;
            }
            break;

        default:
            DETECT_WORKER_ERROR("Unsupported Protocol[%d]", pNewSession->stFlowKey.eProtocol);
            iRet = AGENT_E_PARA;
    }
    SOCKET_UNLOCK();

    delete pstSendMsg;
    pstSendMsg = NULL;


    return iRet;
}

// 添加探测任务, FlowManage使用.
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

    if (stNewFlow.eProtocol != stCfg.eProtocol)  // 检查flow的协议是否与当前worker匹配
    {
        DETECT_WORKER_ERROR("New session Protocol do not match this worker");
        return AGENT_E_PARA;
    }

    if ( SAL_INADDR_ANY !=  stCfg.uiSrcIP
        && (stNewFlow.uiSrcIP!= stCfg.uiSrcIP))    // 检查flow的源IP是否与当前worker匹配. stProtocol.uiSrcIP为0表示匹配任意IP.
    {
        DETECT_WORKER_ERROR("New session SrcIP do not match this worker. New Session IP:[%s]",
                sal_inet_ntoa(stNewFlow.uiSrcIP));

        DETECT_WORKER_ERROR("But this worker IP:[%s]", sal_inet_ntoa(stCfg.uiSrcIP));
        return AGENT_E_PARA;
    }

    if (stNewFlow.uiDscp > AGENT_MAX_DSCP_VALUE)  // 检查flow的dscp是否合法
    {
        DETECT_WORKER_ERROR("New session dscp[%d] is bigger than the max value[%d]", stNewFlow.uiDscp, AGENT_MAX_DSCP_VALUE);
        return AGENT_E_PARA;
    }


    // 入参检查,根据协议类型区分检查.
    switch (stNewFlow.eProtocol)
    {
        case AGENT_DETECT_PROTOCOL_UDP:
            break;

        default:
            DETECT_WORKER_ERROR("Unsupported Protocol[%d]", stNewFlow.eProtocol);
            return AGENT_E_PARA;
    }

    // 检查通过.
    stNewSession.stFlowKey = stNewFlow;
    stNewSession.uiSequenceNumber = uiSequenceNumber++; // 获取序列号
    stNewSession.uiSessionState = SESSION_STATE_INITED; // 初始化状态机.


    // 压入会话列表, rx任务收到应答报文后会根据序列号查找会话列表.
    SESSION_LOCK();
    SessionList.push_back(stNewSession);
    SESSION_UNLOCK();

    // 启动探测报文发送
    iRet = TxPacket(&stNewSession);
    if (iRet)
    {
        DETECT_WORKER_WARNING("TX: TxPacket failed[%d]", iRet);

        stNewSession.uiSessionState = SESSION_STATE_SEND_FAIELD;
        iRet = TxUpdateSession(&stNewSession); //刷新状态机.
        if( iRet )
        {
            DETECT_WORKER_WARNING("TX: Tx Update Session[%d]", iRet);
        }
        return iRet;
    }

    return iRet;
}

// 查询探测结果, FlowManage使用.
INT32 DetectWorker_C::PopSession(DetectWorkerSession_S*
                        pOldSession)
{
    INT32 iRet = AGENT_E_NOT_FOUND;


    SESSION_LOCK();
    vector<DetectWorkerSession_S>::iterator pSession;

    for(pSession = SessionList.begin(); pSession != SessionList.end(); pSession++)
    {
        if ( (SESSION_STATE_SEND_FAIELD == pSession->uiSessionState )
           ||(SESSION_STATE_WAITING_REPLY == pSession->uiSessionState )
           ||(SESSION_STATE_WAITING_CHECK == pSession->uiSessionState ))  // 已经收到报文或者正在等待应答报文
        {
            if (SESSION_STATE_WAITING_REPLY == pSession->uiSessionState)  // 此时尚未收到应答报文意味着超时.
            {
                struct timeval tm;
                pSession->uiSessionState = SESSION_STATE_TIMEOUT;

                sal_memset(&tm, 0, sizeof(tm));
                gettimeofday(&tm,NULL); // 获取当前时间
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

