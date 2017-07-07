
//#include <Python.h>
#include <fstream>

using namespace std;

#include "Sal.h"
#include "Log.h"
#include "AgentJsonAPI.h"
#include "GetLocalCfg.h"

#define SERVER_ANT_CFG_FILE_NAME "ServerAntAgent.cfg"
#define SERVER_ANT_CFG_FILE_PATH "/etc/ServerAntAgent/"

INT32 GetLocalCfg(ServerAntAgentCfg_C * pcCfg)
{

#if 1   // boost ��ʽ����json�����ļ�.

    INT32  iRet = AGENT_OK;

    // �������ļ�
    stringstream ssCfgFileName;
    ifstream ifsAgentCfg;

    ssCfgFileName << SERVER_ANT_CFG_FILE_NAME;
    // �����ڵ�ǰĿ¼���ұ��������ļ�
    ifsAgentCfg.open(ssCfgFileName.str().c_str());
    if ( ifsAgentCfg.fail() )
    {
        INIT_INFO("No cfg file[%s] in current dir, trying [%s] ...", SERVER_ANT_CFG_FILE_NAME, SERVER_ANT_CFG_FILE_PATH);

        //������SERVER_ANT_CFG_FILE_PATHĿ¼���ұ��������ļ�
        ssCfgFileName.clear();
        ssCfgFileName.str("");
        ssCfgFileName << SERVER_ANT_CFG_FILE_PATH << SERVER_ANT_CFG_FILE_NAME;
        ifsAgentCfg.open(ssCfgFileName.str().c_str());
        if ( ifsAgentCfg.fail() )
        {
            INIT_ERROR("Can't open cfg file[%s]", ssCfgFileName.str().c_str());
            return AGENT_E_ERROR;
        }
    }
    INIT_INFO("Using cfg file[%s]", ssCfgFileName.str().c_str());

    // ��ȡ�ļ�����
    string strCfgJsonData((istreambuf_iterator<char>(ifsAgentCfg)),
                          (istreambuf_iterator<char>()));

    // �ر��ļ�
    ifsAgentCfg.close();

    iRet = ParserLocalCfg(strCfgJsonData.c_str(), pcCfg);
    if (iRet)
    {
        INIT_ERROR("ParserLocalCfg failed[%d]", iRet);
        return AGENT_E_ERROR;
    }
#else   // python ��ʽ
    char * pStr = NULL;
    INT64  lPort = 0;
    INT32  iRet = AGENT_OK;

    PyObject *pPyName,*pPyModule,*pPyDict,*pPyFunc,*pPyArgs;
    PyObject *pPyRet;


    // ��ʼ��Python����
    Py_Initialize();

    // ���Python��ʼ���Ƿ�ɹ�.
    if (!Py_IsInitialized())
    {
        INIT_ERROR("Python Initialized failed");
        return AGENT_E_ERROR;
    }

    // C++ֱ�ӵ���python����,׼������
    // ����Python��������
    PyRun_SimpleString("import sys");
    // ����py�ű���·��
    PyRun_SimpleString("sys.path.append('./')");

    // C++����python�ű�,Ȼ�����py�ű��еĺ���.
    // C��ʽ�ַ���תPy��ʽ�ַ���.
    pPyName = PyString_FromString("AgentLocalCfgFileParser");
    // ����ű�AgentLocalCfgFileParser.py
    pPyModule = PyImport_Import(pPyName);
    if ( !pPyModule )
    {
        INIT_ERROR("Python Import AgentLocalCfgFileParser.py Failed");
        return AGENT_E_ERROR;
    }

    // ��ȡ���ű�
    pPyDict = PyModule_GetDict(pPyModule);
    if ( !pPyDict )
    {
        INIT_ERROR("Python GetDict Failed");
        return AGENT_E_ERROR;
    }

    // ׼�������Ĳ���, ���ε����������, ���Բ�������Ϊ0
    pPyArgs = PyTuple_New(0);

    //  PyObject* Py_BuildValue(char *format, ...)
    //  ��C++�ı���ת����һ��Python���󡣵���Ҫ��
    //  C++���ݱ�����Pythonʱ���ͻ�ʹ������������˺���
    //  �е�����C��printf������ʽ��ͬ�����õĸ�ʽ��
    //  s ��ʾ�ַ�����
    //  i ��ʾ���ͱ�����
    //  f ��ʾ��������
    //  O ��ʾһ��Python����

    // ������
    //PyTuple_SetItem(pArgs, 0, Py_BuildValue("l",3));
    //PyTuple_SetItem(pArgs, 1, Py_BuildValue("l",4));

    // ��ȡServerAntServerIP
    // �ҵ�python�ű��еĺ���.
    pPyFunc = PyDict_GetItemString(pPyDict, "GetServerAntServerIP");
    if ( !pPyFunc || !PyCallable_Check(pPyFunc) )
    {
        INIT_ERROR("Python Can't Find Function [GetServerAntServerIP]");
        return AGENT_E_ERROR;
    }

    // ���ú���
    pPyRet = PyObject_CallObject(pPyFunc, pPyArgs);

    //PyArg_Parse(pPyRet, "s", pStr);
    // Py�ַ���ת����C��ʽ���ַ���.
    pStr = PyString_AsString(pPyRet);
    INIT_INFO("GetServerAntServerIP:[%s]\n", pStr);


    // ��ȡServerAntServerPort
    // �ҵ�python�ű��еĺ���.
    pPyFunc = PyDict_GetItemString(pPyDict, "GetServerAntServerPort");
    if ( !pPyFunc || !PyCallable_Check(pPyFunc) )
    {
        INIT_ERROR("Python Can't Find Function [GetServerAntServerPort]");
        return AGENT_E_ERROR;
    }

    // ���ú���
    pPyRet = PyObject_CallObject(pPyFunc, pPyArgs);
    // Py INT32��ʽת����C��ʽ��Long.
    lPort = PyInt_AsLong(pPyRet);

    INIT_INFO("GetServerAntServerPort:[%u]\n", lPort);

    // ˢ�����ö���
    iRet = pcCfg->SetServerAddress(sal_inet_aton(pStr), lPort);
    if ( iRet)
    {
        INIT_ERROR("SetServerAddress failed[%d]", iRet);
        return iRet;
    }

    // �ر�Python
    Py_Finalize();
#endif

    return AGENT_OK;
}


