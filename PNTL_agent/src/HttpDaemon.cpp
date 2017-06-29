
#include <stdio.h>
#include <stdlib.h>
using namespace std;

#include "Sal.h"
#include "AgentCommon.h"
#include "Log.h"

#include "HttpDaemon.h"

#define GET             0
#define POST            1

#define POSTBUFFERSIZE  512

// 链接信息
struct connection_info_struct
{
    INT32 connectiontype;
    string *answerstring;
    struct MHD_PostProcessor *postprocessor;
    
    HttpDaemon_C * pcHttpDaemon;
};

#define HTTP_DAEMON_LOG_TMPBUF_SIZE 512                     /* 日志每一条语句支持的最大长度 */

void HttpDaemonLogCallback(void *cls, const char *fm, va_list ap)
{
    HttpDaemon_C * pcHttpDaemon =  (HttpDaemon_C *)cls;    // 管理本任务的对象.

    char            acLogBuffer[HTTP_DAEMON_LOG_TMPBUF_SIZE];    // 优先使用栈中的buffer, 小而快

    sal_memset(acLogBuffer, 0, sizeof(acLogBuffer));
    vsnprintf(acLogBuffer, sizeof(acLogBuffer) - 1, fm, ap);

    HTTP_DAEMON_ERROR("http server on port[%d] error: %s", pcHttpDaemon->GetCurrentServerPort(), acLogBuffer);
}

// 发送页面信息
INT32 send_page (struct MHD_Connection *connection, const char *page)
{
    INT32 ret;
    struct MHD_Response *response;
    
    response =MHD_create_response_from_buffer (strlen (page), (void *) page,
                MHD_RESPMEM_PERSISTENT);
    if (!response)
    {
        HTTP_DAEMON_ERROR("No Enough Memory for response");
        return MHD_NO;
    }
    
    ret = MHD_queue_response (connection, MHD_HTTP_OK, response);
    MHD_destroy_response (response);

    return ret;
}

// 连接销毁时释放资源
void request_completed (void *cls, struct MHD_Connection *connection,
                   void **con_cls, enum MHD_RequestTerminationCode toe)
{
    struct connection_info_struct *con_info = (connection_info_struct *) *con_cls;

    if (NULL == con_info)
        return;

    string response = "";
	
    if (con_info->connectiontype == POST)
    {
        MHD_destroy_post_processor (con_info->postprocessor);
        if (con_info->answerstring)
			response = * con_info->answerstring;
            delete con_info->answerstring;
    }

    delete con_info;
    *con_cls = NULL;
	INT32 index = response.find("exit");
	if (-1 != index)
	{
	    HTTP_DAEMON_WARNING("exit current process.");
		exit(0);
	}
}

// 业务处理, 每次处理一个key.
INT32 iterate_post (void *coninfo_cls, enum MHD_ValueKind kind, const char *key,
              const char *filename, const char *content_type,
              const char *transfer_encoding, const char *data, uint64_t off,
              size_t size)
{
    INT32 iRet = AGENT_OK;
    struct connection_info_struct *con_info = (connection_info_struct *)coninfo_cls;

    //HTTP_DAEMON_WARNING("key:[%s],value[%s],size[%u]", key, data, size);
    
    if (size > 0)
    {
        iRet = con_info->pcHttpDaemon->ProcessPostIterate(key, data, size, con_info->answerstring);
        if(AGENT_OK == iRet)
        {
            // 继续处理本次post的其他key.
            return MHD_YES;
        }
        else
        {
            // 终止本次post处理.
            HTTP_DAEMON_ERROR("ProcessPostIterate key:[%s],value[%s],size[%u], failed[%d]", key, data, size, iRet);
            return MHD_NO;
        }
    }
    else
    {
        // 有些client在post数据结尾忘记添加结束字符"\n", 销毁链接前daemon会补一个结束字符下来, 直接忽略即可.
        // HTTP_DAEMON_WARNING("Ignore this key:[%s],value[%s],size[%u]", key, data, size);
        return MHD_YES;
    }
}

// http daemon 请求处理函数
/**
 * A client has requested the given url using the given method
 * (#MHD_HTTP_METHOD_GET, #MHD_HTTP_METHOD_PUT,
 * #MHD_HTTP_METHOD_DELETE, #MHD_HTTP_METHOD_POST, etc).  The callback
 * must call MHD callbacks to provide content to give back to the
 * client and return an HTTP status code (i.e. #MHD_HTTP_OK,
 * #MHD_HTTP_NOT_FOUND, etc.).
 *
 * @param cls argument given together with the function
 *        pointer when the handler was registered with MHD
 * @param url the requested url
 * @param method the HTTP method used (#MHD_HTTP_METHOD_GET,
 *        #MHD_HTTP_METHOD_PUT, etc.)
 * @param version the HTTP version string (i.e.
 *        #MHD_HTTP_VERSION_1_1)
 * @param upload_data the data being uploaded (excluding HEADERS,
 *        for a POST that fits into memory and that is encoded
 *        with a supported encoding, the POST data will NOT be
 *        given in upload_data and is instead available as
 *        part of #MHD_get_connection_values; very large POST
 *        data *will* be made available incrementally in
 *        @a upload_data)
 * @param upload_data_size set initially to the size of the
 *        @a upload_data provided; the method must update this
 *        value to the number of bytes NOT processed;
 * @param con_cls pointer that the callback can set to some
 *        address and that will be preserved by MHD for future
 *        calls for this request; since the access handler may
 *        be called many times (i.e., for a PUT/POST operation
 *        with plenty of upload data) this allows the application
 *        to easily associate some request-specific state.
 *        If necessary, this state can be cleaned up in the
 *        global #MHD_RequestCompletedCallback (which
 *        can be set with the #MHD_OPTION_NOTIFY_COMPLETED).
 *        Initially, `*con_cls` will be NULL.
 * @return #MHD_YES if the connection was handled successfully,
 *         #MHD_NO if the socket must be closed due to a serios
 *         error while handling the request
 */
INT32 HttpDaemonHandlerCallback (void *cls,
                              struct MHD_Connection *connection,
                              const char *url,
                              const char *method,
                              const char *version,
                              const char *upload_data,
                              size_t *upload_data_size,
                              void **con_cls)
{
    HttpDaemon_C * pcHttpDaemon =  (HttpDaemon_C *)cls;    // 管理本任务的对象.

    // header 信息处理完毕, 通知handler准备.
    // connection->state 为 MHD_CONNECTION_HEADERS_PROCESSED 时第一次调用本函数, 此时upload_data中无数据.
    // 同时daemon本身会根据client请求发送http 100 (continue)响应. 部分client post长数据前会要求Server端详细continue.
    
    // 使用*con_cls作为判断标记, 本连接第一次调用时*con_cls为空.
    if (NULL == *con_cls)
    {
        struct connection_info_struct *con_info;

        // 创建本链接的私有数据.
        con_info = new connection_info_struct;
        if (NULL == con_info)
        {
            HTTP_DAEMON_ERROR("No Enough Memory for connection_info_struct");
            return MHD_NO;
        }
        
        sal_memset(con_info, 0, sizeof(connection_info_struct));
        
        con_info->answerstring = new string;
        if (NULL == con_info->answerstring)
        {
            HTTP_DAEMON_ERROR("No Enough Memory for string");
            return MHD_NO;
        }
        
        // 管理本daemon的对象
        con_info->pcHttpDaemon = pcHttpDaemon;
        
        if (0 == sal_strcmp (method, MHD_HTTP_METHOD_POST))
        {
            // 创建post processor, 后续收到数据后使用该processor处理.
            con_info->postprocessor =
                MHD_create_post_processor (connection, POSTBUFFERSIZE,
                                           iterate_post, (void *) con_info);

            if (NULL == con_info->postprocessor)
            {
                HTTP_DAEMON_ERROR("No Enough Memory for postprocessor");
                delete con_info;
                return MHD_NO;
            }
            con_info->connectiontype = POST;
        }
        else
        {
            con_info->connectiontype = GET;
        }

        *con_cls = (void *) con_info;
        
        // 通知client continue.
        return MHD_YES;
    }

    // 处理body信息.    
    if (0 == sal_strcmp (method, MHD_HTTP_METHOD_POST))
    {
        // 获取第一次提交数据时的信息.
        struct connection_info_struct *con_info = (connection_info_struct *) *con_cls;
        
        // connection->state 为 MHD_CONNECTION_CONTINUE_SENT 时调用, upload_data中有数据, 通知handler处理.
        if (*upload_data_size != 0)
        {
            INT32 iRet;
            // 处理client本次提交的数据, 遍历key, 每个key调用一次iterate_post函数解析.
            // post提交的数据格式"key1=data1&key2=data2\n"
            iRet = MHD_post_process (con_info->postprocessor, upload_data,
                            *upload_data_size);
            if (MHD_YES != iRet)
            {
                HTTP_DAEMON_ERROR("MHD post process failed. Post Buffer Size[%u], Get data size[%u]", POSTBUFFERSIZE, *upload_data_size);
            }
            
            *upload_data_size = 0;

            return MHD_YES;
        }
        // connection->state 为 MHD_CONNECTION_FOOTERS_RECEIVED 时调用, upload_data中无数据, 通知handler发送响应数据.
        else if (0 != con_info->answerstring->size())
        {
            // 返回处理结果.
            return send_page (connection, con_info->answerstring->c_str());
        }
        else
        {
            // 没有处理结果时,默认返回处理失败.
            return send_page(connection, pcHttpDaemon->pcResponcePageError);
        }
    }
    else if (0 == sal_strcmp (method, MHD_HTTP_METHOD_GET))
    {
        // connection->state 为 MHD_CONNECTION_FOOTERS_RECEIVED 时调用, upload_data中无数据, 通知handler发送响应数据.
        // 根据url信息给出响应数据. client请求"http://10.78.221.45:1200/xxx?type=xml"时, 本函数url参数中保存的是"/xxx"
        // 其他参数以key/data形式提供. 如上例中的"type":"xml".

        /*
        // 获取参数, 函数会遍历key/data回调get_url_args()函数, url_args是get_url_args()函数的参数, 一般用于保存结果.
        if (MHD_get_connection_values (connection, MHD_GET_ARGUMENT_KIND, 
                           get_url_args, &url_args) < 0) {
            return send_page(connection, pcHttpDaemon->pcResponcePageError);
        }

        // 根据url, url_args 生成响应数据.
        ProcessGetAction(url, url_args, respdata);

        // 将响应数据发送给client.        
        response = MHD_create_response_from_buffer (strlen (me), me, MHD_RESPMEM_MUST_FREE);
        MHD_queue_response (connection, MHD_HTTP_OK, response);
        MHD_destroy_response (response);
        */
        
        // 暂不支持get操作
        HTTP_DAEMON_WARNING("Don't Support Get Now, url[%s]", url);
        return send_page(connection, pcHttpDaemon->pcResponcePageUnsupported);
    }

    // 异常分支, 返回错误提示.
    HTTP_DAEMON_WARNING("Unsupported Request");
    return send_page(connection, pcHttpDaemon->pcResponcePageUnsupported);
}

// 构造函数, 填充默认值.
HttpDaemon_C::HttpDaemon_C()
{
    HTTP_DAEMON_INFO("Creat a new Http Daemon.");

    uiPort = 0;
    pstDaemon = NULL;

    pcResponcePageOK = "<html><head><title>Info</title></head><body>Process Request Sucess</body></html>";
    pcResponcePageError = "<html><head><title>Error</title></head><body>Process Request Failed</body></html>";
    pcResponcePageUnsupported = "<html><head><title>Error</title></head><body>Unsupported Request</body></html>";
    
}

// 析构函数, 释放必要资源.
HttpDaemon_C::~HttpDaemon_C()
{
    HTTP_DAEMON_INFO("Destroy an old Http Daemon.");


     if (pstDaemon) // 停止http daemon
        MHD_stop_daemon (pstDaemon);
     pstDaemon = NULL;
}

// 根据入参启动http daemon, 支持重复启动
INT32 HttpDaemon_C::StartHttpDaemon(UINT32 uiNewPort)
{
    // 端口号入参检查
    if (0 == uiNewPort)
    {
        HTTP_DAEMON_ERROR("The New Port is [%d].", 
            uiNewPort);
        return AGENT_E_PARA;
    }
    
    HTTP_DAEMON_INFO("Start HttpDaemon on Port [%d].", uiNewPort);
    
    // 如果http daemon已经启动, 则先停止.
    if (NULL != pstDaemon)
    {
        MHD_stop_daemon (pstDaemon);
        pstDaemon = NULL;
    }

    // 使用新端口号启动http daemon.
    /**
     * Start a webserver on the given port.  Variadic version of
     * #MHD_start_daemon_va.
     *
     * @param flags combination of `enum MHD_FLAG` values
     * @param port port to bind to
     * @param apc callback to call to check which clients
     *        will be allowed to connect; you can pass NULL
     *        in which case connections from any IP will be
     *        accepted
     * @param apc_cls extra argument to @a apc
     * @param dh handler called for all requests (repeatedly)
     * @param dh_cls extra argument to @a dh
     * @return NULL on error, handle to daemon on success
     * @ingroup event
     */
    pstDaemon = MHD_start_daemon (
                                MHD_USE_SELECT_INTERNALLY | MHD_USE_DEBUG | MHD_USE_POLL,
                                uiNewPort,
                                0, 0, 
                                &HttpDaemonHandlerCallback, this, 
                                MHD_OPTION_NOTIFY_COMPLETED, request_completed, NULL,
                                MHD_OPTION_EXTERNAL_LOGGER, HttpDaemonLogCallback, this,
                                MHD_OPTION_END
                                );
    if (NULL == pstDaemon)
    {
        HTTP_DAEMON_ERROR("Http Daemon Init Failed on Port [%d].", uiNewPort);
        return AGENT_E_SOCKET;
    }
    
    // 启动成功, 刷新端口信息.
    uiPort = uiNewPort;    
    return AGENT_OK;
}

// 停止http daemon
INT32 HttpDaemon_C::StopHttpDaemon()
{
    if (NULL != pstDaemon)
    {
        HTTP_DAEMON_INFO("Stop HttpDaemon Now.");
        MHD_stop_daemon (pstDaemon);
        pstDaemon = NULL;
    }
    return AGENT_OK;
}

// Http Server Daemon POST操作处理函数
INT32 HttpDaemon_C::ProcessPostIterate(const char * pcKey, const char * pcData, UINT32 uiDataSize, string * pstrResponce)
{

    HTTP_DAEMON_WARNING("PostIterate use default func. key:[%s],value[%s],size[%u]", pcKey, pcData, uiDataSize);

    // 入参检查
    if (NULL ==  pcKey || NULL == pcData || NULL ==  pstrResponce) 
    {
         HTTP_DAEMON_ERROR("NULL Pointer for String");
         return AGENT_E_PARA;
    }

    // * pcResponce 为本次Post操作公用, 一次post操作可能会提交多个key/data.
    // post操作处理完毕后会将pcResponce中的信息返回给客户.
    // 根据与client端的约定,可以每个key对应一个结果或者整个post对应一个结果.
    
    if (0 == sal_strcmp (pcKey, "XXX"))
    {
        // 整个post返回一个结果.
        (* pstrResponce) = pcResponcePageError;

        // 整个post返回一个字符串, 字符串由多个key处理结果连接起来.
        //(* pstrResponce) += pcResponcePageError;

        
        // 一旦返回错误, 会终止本次post处理, 忽略尚未处理的key/data值.
        return AGENT_E_ERROR;
    }
    else
    {
        // 整个post返回一个结果.
        (* pstrResponce) = pcResponcePageOK;

        // 整个post返回一个字符串, 字符串由多个key处理结果连接起来.
        //(* pstrResponce) += pcResponcePageOK;
        
        return AGENT_OK;
    }
}




