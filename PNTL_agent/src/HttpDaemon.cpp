
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

// ������Ϣ
struct connection_info_struct
{
    INT32 connectiontype;
    string *answerstring;
    struct MHD_PostProcessor *postprocessor;
    
    HttpDaemon_C * pcHttpDaemon;
};

#define HTTP_DAEMON_LOG_TMPBUF_SIZE 512                     /* ��־ÿһ�����֧�ֵ���󳤶� */

void HttpDaemonLogCallback(void *cls, const char *fm, va_list ap)
{
    HttpDaemon_C * pcHttpDaemon =  (HttpDaemon_C *)cls;    // ��������Ķ���.

    char            acLogBuffer[HTTP_DAEMON_LOG_TMPBUF_SIZE];    // ����ʹ��ջ�е�buffer, С����

    sal_memset(acLogBuffer, 0, sizeof(acLogBuffer));
    vsnprintf(acLogBuffer, sizeof(acLogBuffer) - 1, fm, ap);

    HTTP_DAEMON_ERROR("http server on port[%d] error: %s", pcHttpDaemon->GetCurrentServerPort(), acLogBuffer);
}

// ����ҳ����Ϣ
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

// ��������ʱ�ͷ���Դ
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

// ҵ����, ÿ�δ���һ��key.
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
            // ����������post������key.
            return MHD_YES;
        }
        else
        {
            // ��ֹ����post����.
            HTTP_DAEMON_ERROR("ProcessPostIterate key:[%s],value[%s],size[%u], failed[%d]", key, data, size, iRet);
            return MHD_NO;
        }
    }
    else
    {
        // ��Щclient��post���ݽ�β������ӽ����ַ�"\n", ��������ǰdaemon�Ჹһ�������ַ�����, ֱ�Ӻ��Լ���.
        // HTTP_DAEMON_WARNING("Ignore this key:[%s],value[%s],size[%u]", key, data, size);
        return MHD_YES;
    }
}

// http daemon ��������
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
    HttpDaemon_C * pcHttpDaemon =  (HttpDaemon_C *)cls;    // ��������Ķ���.

    // header ��Ϣ�������, ֪ͨhandler׼��.
    // connection->state Ϊ MHD_CONNECTION_HEADERS_PROCESSED ʱ��һ�ε��ñ�����, ��ʱupload_data��������.
    // ͬʱdaemon��������client������http 100 (continue)��Ӧ. ����client post������ǰ��Ҫ��Server����ϸcontinue.
    
    // ʹ��*con_cls��Ϊ�жϱ��, �����ӵ�һ�ε���ʱ*con_clsΪ��.
    if (NULL == *con_cls)
    {
        struct connection_info_struct *con_info;

        // ���������ӵ�˽������.
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
        
        // ����daemon�Ķ���
        con_info->pcHttpDaemon = pcHttpDaemon;
        
        if (0 == sal_strcmp (method, MHD_HTTP_METHOD_POST))
        {
            // ����post processor, �����յ����ݺ�ʹ�ø�processor����.
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
        
        // ֪ͨclient continue.
        return MHD_YES;
    }

    // ����body��Ϣ.    
    if (0 == sal_strcmp (method, MHD_HTTP_METHOD_POST))
    {
        // ��ȡ��һ���ύ����ʱ����Ϣ.
        struct connection_info_struct *con_info = (connection_info_struct *) *con_cls;
        
        // connection->state Ϊ MHD_CONNECTION_CONTINUE_SENT ʱ����, upload_data��������, ֪ͨhandler����.
        if (*upload_data_size != 0)
        {
            INT32 iRet;
            // ����client�����ύ������, ����key, ÿ��key����һ��iterate_post��������.
            // post�ύ�����ݸ�ʽ"key1=data1&key2=data2\n"
            iRet = MHD_post_process (con_info->postprocessor, upload_data,
                            *upload_data_size);
            if (MHD_YES != iRet)
            {
                HTTP_DAEMON_ERROR("MHD post process failed. Post Buffer Size[%u], Get data size[%u]", POSTBUFFERSIZE, *upload_data_size);
            }
            
            *upload_data_size = 0;

            return MHD_YES;
        }
        // connection->state Ϊ MHD_CONNECTION_FOOTERS_RECEIVED ʱ����, upload_data��������, ֪ͨhandler������Ӧ����.
        else if (0 != con_info->answerstring->size())
        {
            // ���ش�����.
            return send_page (connection, con_info->answerstring->c_str());
        }
        else
        {
            // û�д�����ʱ,Ĭ�Ϸ��ش���ʧ��.
            return send_page(connection, pcHttpDaemon->pcResponcePageError);
        }
    }
    else if (0 == sal_strcmp (method, MHD_HTTP_METHOD_GET))
    {
        // connection->state Ϊ MHD_CONNECTION_FOOTERS_RECEIVED ʱ����, upload_data��������, ֪ͨhandler������Ӧ����.
        // ����url��Ϣ������Ӧ����. client����"http://10.78.221.45:1200/xxx?type=xml"ʱ, ������url�����б������"/xxx"
        // ����������key/data��ʽ�ṩ. �������е�"type":"xml".

        /*
        // ��ȡ����, ���������key/data�ص�get_url_args()����, url_args��get_url_args()�����Ĳ���, һ�����ڱ�����.
        if (MHD_get_connection_values (connection, MHD_GET_ARGUMENT_KIND, 
                           get_url_args, &url_args) < 0) {
            return send_page(connection, pcHttpDaemon->pcResponcePageError);
        }

        // ����url, url_args ������Ӧ����.
        ProcessGetAction(url, url_args, respdata);

        // ����Ӧ���ݷ��͸�client.        
        response = MHD_create_response_from_buffer (strlen (me), me, MHD_RESPMEM_MUST_FREE);
        MHD_queue_response (connection, MHD_HTTP_OK, response);
        MHD_destroy_response (response);
        */
        
        // �ݲ�֧��get����
        HTTP_DAEMON_WARNING("Don't Support Get Now, url[%s]", url);
        return send_page(connection, pcHttpDaemon->pcResponcePageUnsupported);
    }

    // �쳣��֧, ���ش�����ʾ.
    HTTP_DAEMON_WARNING("Unsupported Request");
    return send_page(connection, pcHttpDaemon->pcResponcePageUnsupported);
}

// ���캯��, ���Ĭ��ֵ.
HttpDaemon_C::HttpDaemon_C()
{
    HTTP_DAEMON_INFO("Creat a new Http Daemon.");

    uiPort = 0;
    pstDaemon = NULL;

    pcResponcePageOK = "<html><head><title>Info</title></head><body>Process Request Sucess</body></html>";
    pcResponcePageError = "<html><head><title>Error</title></head><body>Process Request Failed</body></html>";
    pcResponcePageUnsupported = "<html><head><title>Error</title></head><body>Unsupported Request</body></html>";
    
}

// ��������, �ͷű�Ҫ��Դ.
HttpDaemon_C::~HttpDaemon_C()
{
    HTTP_DAEMON_INFO("Destroy an old Http Daemon.");


     if (pstDaemon) // ֹͣhttp daemon
        MHD_stop_daemon (pstDaemon);
     pstDaemon = NULL;
}

// �����������http daemon, ֧���ظ�����
INT32 HttpDaemon_C::StartHttpDaemon(UINT32 uiNewPort)
{
    // �˿ں���μ��
    if (0 == uiNewPort)
    {
        HTTP_DAEMON_ERROR("The New Port is [%d].", 
            uiNewPort);
        return AGENT_E_PARA;
    }
    
    HTTP_DAEMON_INFO("Start HttpDaemon on Port [%d].", uiNewPort);
    
    // ���http daemon�Ѿ�����, ����ֹͣ.
    if (NULL != pstDaemon)
    {
        MHD_stop_daemon (pstDaemon);
        pstDaemon = NULL;
    }

    // ʹ���¶˿ں�����http daemon.
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
    
    // �����ɹ�, ˢ�¶˿���Ϣ.
    uiPort = uiNewPort;    
    return AGENT_OK;
}

// ֹͣhttp daemon
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

// Http Server Daemon POST����������
INT32 HttpDaemon_C::ProcessPostIterate(const char * pcKey, const char * pcData, UINT32 uiDataSize, string * pstrResponce)
{

    HTTP_DAEMON_WARNING("PostIterate use default func. key:[%s],value[%s],size[%u]", pcKey, pcData, uiDataSize);

    // ��μ��
    if (NULL ==  pcKey || NULL == pcData || NULL ==  pstrResponce) 
    {
         HTTP_DAEMON_ERROR("NULL Pointer for String");
         return AGENT_E_PARA;
    }

    // * pcResponce Ϊ����Post��������, һ��post�������ܻ��ύ���key/data.
    // post����������Ϻ�ὫpcResponce�е���Ϣ���ظ��ͻ�.
    // ������client�˵�Լ��,����ÿ��key��Ӧһ�������������post��Ӧһ�����.
    
    if (0 == sal_strcmp (pcKey, "XXX"))
    {
        // ����post����һ�����.
        (* pstrResponce) = pcResponcePageError;

        // ����post����һ���ַ���, �ַ����ɶ��key��������������.
        //(* pstrResponce) += pcResponcePageError;

        
        // һ�����ش���, ����ֹ����post����, ������δ�����key/dataֵ.
        return AGENT_E_ERROR;
    }
    else
    {
        // ����post����һ�����.
        (* pstrResponce) = pcResponcePageOK;

        // ����post����һ���ַ���, �ַ����ɶ��key��������������.
        //(* pstrResponce) += pcResponcePageOK;
        
        return AGENT_OK;
    }
}




