
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

#if 1   // boost 方式解析json配置文件.

    INT32  iRet = AGENT_OK;

    // 打开配置文件
    stringstream ssCfgFileName;
    ifstream ifsAgentCfg;

    ssCfgFileName << SERVER_ANT_CFG_FILE_NAME;
    // 尝试在当前目录查找本地配置文件
    ifsAgentCfg.open(ssCfgFileName.str().c_str());
    if ( ifsAgentCfg.fail() )
    {
        INIT_INFO("No cfg file[%s] in current dir, trying [%s] ...", SERVER_ANT_CFG_FILE_NAME, SERVER_ANT_CFG_FILE_PATH);

        //尝试在SERVER_ANT_CFG_FILE_PATH目录查找本地配置文件
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

    // 读取文件内容
    string strCfgJsonData((istreambuf_iterator<char>(ifsAgentCfg)),
                          (istreambuf_iterator<char>()));

    // 关闭文件
    ifsAgentCfg.close();

    iRet = ParserLocalCfg(strCfgJsonData.c_str(), pcCfg);
    if (iRet)
    {
        INIT_ERROR("ParserLocalCfg failed[%d]", iRet);
        return AGENT_E_ERROR;
    }
#else   // python 方式
    char * pStr = NULL;
    INT64  lPort = 0;
    INT32  iRet = AGENT_OK;

    PyObject *pPyName,*pPyModule,*pPyDict,*pPyFunc,*pPyArgs;
    PyObject *pPyRet;


    // 初始化Python容器
    Py_Initialize();

    // 检查Python初始化是否成功.
    if (!Py_IsInitialized())
    {
        INIT_ERROR("Python Initialized failed");
        return AGENT_E_ERROR;
    }

    // C++直接调用python命令,准备环境
    // 加载Python环境变量
    PyRun_SimpleString("import sys");
    // 加载py脚本的路径
    PyRun_SimpleString("sys.path.append('./')");

    // C++载入python脚本,然后调用py脚本中的函数.
    // C格式字符串转Py格式字符串.
    pPyName = PyString_FromString("AgentLocalCfgFileParser");
    // 载入脚本AgentLocalCfgFileParser.py
    pPyModule = PyImport_Import(pPyName);
    if ( !pPyModule )
    {
        INIT_ERROR("Python Import AgentLocalCfgFileParser.py Failed");
        return AGENT_E_ERROR;
    }

    // 获取符号表
    pPyDict = PyModule_GetDict(pPyModule);
    if ( !pPyDict )
    {
        INIT_ERROR("Python GetDict Failed");
        return AGENT_E_ERROR;
    }

    // 准备函数的参数, 本次调用无需参数, 所以参数个数为0
    pPyArgs = PyTuple_New(0);

    //  PyObject* Py_BuildValue(char *format, ...)
    //  把C++的变量转换成一个Python对象。当需要从
    //  C++传递变量到Python时，就会使用这个函数。此函数
    //  有点类似C的printf，但格式不同。常用的格式有
    //  s 表示字符串，
    //  i 表示整型变量，
    //  f 表示浮点数，
    //  O 表示一个Python对象。

    // 填充参数
    //PyTuple_SetItem(pArgs, 0, Py_BuildValue("l",3));
    //PyTuple_SetItem(pArgs, 1, Py_BuildValue("l",4));

    // 获取ServerAntServerIP
    // 找到python脚本中的函数.
    pPyFunc = PyDict_GetItemString(pPyDict, "GetServerAntServerIP");
    if ( !pPyFunc || !PyCallable_Check(pPyFunc) )
    {
        INIT_ERROR("Python Can't Find Function [GetServerAntServerIP]");
        return AGENT_E_ERROR;
    }

    // 调用函数
    pPyRet = PyObject_CallObject(pPyFunc, pPyArgs);

    //PyArg_Parse(pPyRet, "s", pStr);
    // Py字符串转换成C格式的字符串.
    pStr = PyString_AsString(pPyRet);
    INIT_INFO("GetServerAntServerIP:[%s]\n", pStr);


    // 获取ServerAntServerPort
    // 找到python脚本中的函数.
    pPyFunc = PyDict_GetItemString(pPyDict, "GetServerAntServerPort");
    if ( !pPyFunc || !PyCallable_Check(pPyFunc) )
    {
        INIT_ERROR("Python Can't Find Function [GetServerAntServerPort]");
        return AGENT_E_ERROR;
    }

    // 调用函数
    pPyRet = PyObject_CallObject(pPyFunc, pPyArgs);
    // Py INT32格式转换成C格式的Long.
    lPort = PyInt_AsLong(pPyRet);

    INIT_INFO("GetServerAntServerPort:[%u]\n", lPort);

    // 刷新配置对象
    iRet = pcCfg->SetServerAddress(sal_inet_aton(pStr), lPort);
    if ( iRet)
    {
        INIT_ERROR("SetServerAddress failed[%d]", iRet);
        return iRet;
    }

    // 关闭Python
    Py_Finalize();
#endif

    return AGENT_OK;
}


