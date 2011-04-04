#include "common.h"
#include <iostream>
#include <time.h>
#include "comutil.h"

#include "OPCClient.h"
#include "OPCHost.h"
#include "OPCServer.h"
#include "OPCGroup.h"
#include "GroupManager.h"
#include "AsyncUpdateHandler.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/backends/bec.file.h>
#include <pantheios/inserters/integer.hpp>
#include <pantheios/frontends/fe.simple.h>

using namespace pantheios;
using namespace std;

bool gsbLoggingInitialised = false;
char gscLogFilePath[MAX_PATH];

COPCHost *gspHost = NULL;
COPCServer *gspOpcServer = NULL;
CAtlArray<CString> gsoOpcServerAddressSpace;

AsyncUpdateHandler updateHandler;
TransactionCompleteHandler transactionHandler;
GroupManager gsoGroupManager(updateHandler);

CString gstrLastError = "No errors reported";


const char* const GetLogFilePath()
{
	time_t tmNow = time(NULL);
	
	char cTmString[MAX_PATH];
	memset(cTmString, MAX_PATH, 0);
	strftime(cTmString, MAX_PATH, "%Y_%m_%d___%H_%M_%S", localtime(&tmNow));

	memset(gscLogFilePath, 0, MAX_PATH);
	sprintf_s(gscLogFilePath, MAX_PATH, "C:\\TEMP\\%s___OPC-JNI-DLL.log", cTmString);

	std::cout << "logfile ["<< gscLogFilePath <<"]" << std::endl;	
	return gscLogFilePath;
}

void InitialiseLogging()
{
	std::cout << "initialising logging..." << std::endl;
	if(gsbLoggingInitialised)
	{
		return;
	}

	try
	{
		if(pantheios::pantheios_init() < 0)
		{
			std::cout << "(DLL: ERROR) failed to initialise logger" << std::endl;
		}
		else
		{
			pantheios_be_file_setFilePath(GetLogFilePath(), PANTHEIOS_BEID_ALL);
		}
		log_NOTICE("InitialiseLogging: logging initialised");
		
		gsbLoggingInitialised = true;
		std::cout << "logging initialised" << std::endl;
	}
	catch(...)
	{
		logputs(pantheios::emergency, "Unexpected unknown error occurred whilst initialising logging");
		std::cout << "logging failed" << std::endl;
	}

	//pantheios_fe_simple_setSeverityCeiling(PANTHEIOS_SEV_WARNING);
}

extern "C"
{
	__declspec(dllexport) void __cdecl init(const char* const pHost, const char* const pServer)
	{
		log_NOTICE("Initialising with host [",pHost,"] port [",pServer,"]");

		InitialiseLogging();

		COPCClient::init();
		log_NOTICE("init - intialised OPCClient class");

		gspHost = COPCClient::makeHost(pHost);
		log_NOTICE("init: made host [", pHost,"]");

		gspOpcServer = gspHost->connectDAServer(pServer);
		log_NOTICE("init: connected opcServer [", pServer,"]");
	}

	__declspec(dllexport) void __cdecl end()
	{
		log_NOTICE("Ending client session");

		COPCClient::stop();
		log_NOTICE("stop - called for OPCClient class");
	}

	__declspec(dllexport) const unsigned long __cdecl createGroup(const char* const pGroupName, const unsigned long requestedRefreshRate)
	{
		log_NOTICE("createGroup called, group name [", pGroupName,"] requested refresh rate [", ((pantheios::integer)requestedRefreshRate),"]");

		unsigned long actualRefreshRate = gsoGroupManager.CreateGroup(pGroupName, requestedRefreshRate);

		log_NOTICE("createGroup completed, for group name [", pGroupName,"] actual refresh rate [",((pantheios::integer)actualRefreshRate),"]");
		return actualRefreshRate;
	} 

	__declspec(dllexport) const bool __cdecl destroyGroup(const char* const pGroupName)
	{
		log_NOTICE("destroyGroup called, group name [", pGroupName,"]");

		bool result = gsoGroupManager.DestroyGroup(pGroupName);

		log_NOTICE("destroyGroup completed, for group name [", pGroupName,"] success [",(result?"Y":"N"),"]");
		return result;
	} 


	__declspec(dllexport) const bool __cdecl addItem(const char* const pGroupName, const char* pItemPath)
	{
		log_NOTICE("addItem called, group name [", pGroupName,"] item name [", pItemPath,"]");

		bool bAdded = gsoGroupManager.AddItem(pGroupName, pItemPath);

		log_NOTICE("addItem completed, for group name [", pGroupName,"] success [",(bAdded?"Y":"N"),"]");
		return bAdded;
	}

	__declspec(dllexport) const bool __cdecl readItemSync(const char* const pGroupName, const char* pItemPath, char* pBuff, const int nBuffSz)
	{
		log_NOTICE("readItemSync called, group [",pGroupName,"] item [",pItemPath,"] initial buffer [", pBuff,"] buffer sz [", pantheios::integer(nBuffSz),"]");

		bool result = gsoGroupManager.ReadItemSync(pGroupName, pItemPath, pBuff, nBuffSz);

		log_NOTICE("readItemSync complete, buffer [", pBuff,"] buffer sz [", pantheios::integer(nBuffSz),"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl readItemAsync(const char* const pGroupName, const char* pItemPath)
	{
		log_NOTICE("readItemAsync called, group [",pGroupName,"] item [",pItemPath,"]");

		bool result = gsoGroupManager.ReadItemAsync(pGroupName, pItemPath);

		log_NOTICE("readItemAsync complete, success [", (result?"Y":"N"),"]");
		return false;
	}


	__declspec(dllexport) const bool __cdecl writeItemSync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		log_NOTICE("writeItemSync called, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"]");
		
		bool result = gsoGroupManager.WriteItemSync(pGroupName, pItemPath, pValue);
		
		log_NOTICE("writeItemSync complete, group [", pGroupName,"] item [", pItemPath,"] success [", (result?"Y":"N"),"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl writeItemAsync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		log_NOTICE("writeItemAsync called, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"]");
		
		bool result = gsoGroupManager.WriteItemAsync(pGroupName, pItemPath, pValue);
		
		log_NOTICE("writeItemAsync completed, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"], success [", (result?"Y":"N"),"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl getItemNames(char* itemsBuffer[], const int nElementLength, const int nNumElements, const int nOffSet)
	{
		log_NOTICE("getItemNames: called, nNumElements [",((pantheios::integer)nNumElements),"] nElementLength [",((pantheios::integer)nElementLength),"] nOffset [", ((pantheios::integer)nOffSet),"]");

		if(gsoOpcServerAddressSpace.IsEmpty())
		{
	  		gspOpcServer->getItemNames(gsoOpcServerAddressSpace);
		}

		log_NOTICE("item names retrived: there are [", pantheios::integer(gsoOpcServerAddressSpace.GetCount()),"] items");

		for(unsigned int i=nOffSet; i<gsoOpcServerAddressSpace.GetCount() && i-nOffSet < nNumElements; i++)
		{
			strcpy_s(itemsBuffer[i-nOffSet], nElementLength, CStringA(gsoOpcServerAddressSpace[i]).GetString());
			log_NOTICE("getItemNames: copying opc item to java buffer, index [", pantheios::integer(i),"] value [", CStringA(gsoOpcServerAddressSpace[i]).GetString(),"]");
		}

		bool result = nNumElements > gsoOpcServerAddressSpace.GetCount() - nOffSet;
		log_NOTICE("getItemNames: result [",(result?"Y":"N"),"]");
		
		return result;
	}

	__declspec(dllexport) void __cdecl getLastError(char* const pErrorBuffer, const int nBuffSz)
	{
		strcpy_s(pErrorBuffer, nBuffSz, gstrLastError);
	}

	__declspec(dllexport) void __cdecl registerAsyncUpdate(updateCallback cb)
	{
		updateHandler.SetCallback(cb);
	}
}