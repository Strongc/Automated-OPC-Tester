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
#include "Utils.h"
#include "ItemValueStruct.h"

using namespace pantheios;
using namespace std;
using namespace Utils;

bool gsbLoggingInitialised = false;
char gscLogFilePath[MAX_PATH];

COPCHost *gspHost = NULL;
COPCServer *gspOpcServer = NULL;
CAtlArray<CString> gsoOpcServerAddressSpace;

AsyncUpdateHandler updateHandler;
TransactionCompleteHandler transactionHandler;
GroupManager gsoGroupManager(updateHandler);

std::string gstrLastError = "No errors reported";


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
		log_NOTICE("init+, host [",pHost,"] server [",pServer,"]");

		try
		{
			InitialiseLogging();

			COPCClient::init();
			log_NOTICE("init - intialised OPCClient class");

			gspHost = COPCClient::makeHost(pHost);
			log_NOTICE("init: made host [", pHost,"]");

			gspOpcServer = gspHost->connectDAServer(pServer);			
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("init: [", gstrLastError,"]");
		}

		log_NOTICE("init-, host [",pHost,"] server [",pServer,"]");
	}

	__declspec(dllexport) void __cdecl end()
	{
		log_NOTICE("end+ ending client session");

		try
		{
			COPCClient::stop();
			log_NOTICE("stop - called for OPCClient class");
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("end: [", gstrLastError,"]");
		}

		log_NOTICE("end-");
	}

	__declspec(dllexport) const unsigned long __cdecl createGroup(const char* const pGroupName, const unsigned long requestedRefreshRate)
	{
		log_NOTICE("createGroup+, group name [", pGroupName,"] requested refresh rate [", ((pantheios::integer)requestedRefreshRate),"]");

		unsigned long actualRefreshRate = 0;
		try
		{		
			actualRefreshRate = gsoGroupManager.CreateGroup(pGroupName, requestedRefreshRate);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("createGroup: [", gstrLastError,"]");
		}

		log_NOTICE("createGroup-, group name [", pGroupName,"] actual refresh rate [",((pantheios::integer)actualRefreshRate),"]");
		return actualRefreshRate;
	} 

	__declspec(dllexport) const bool __cdecl destroyGroup(const char* const pGroupName)
	{
		log_NOTICE("destroyGroup+, group name [", pGroupName,"]");

		bool result = false;
		try
		{
			result = gsoGroupManager.DestroyGroup(pGroupName);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("destroyGroup: [", gstrLastError,"]");
		}

		log_NOTICE("destroyGroup-, group name [", pGroupName,"] success [",(result?"Y":"N"),"]");
		return result;
	} 


	__declspec(dllexport) const bool __cdecl addItem(const char* const pGroupName, const char* pItemPath)
	{
		log_NOTICE("addItem+, group name [", pGroupName,"] item name [", pItemPath,"]");

		bool bAdded = false;
		try
		{
			bAdded = gsoGroupManager.AddItem(pGroupName, pItemPath);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("addItem: [", gstrLastError,"]");
		}

		log_NOTICE("addItem-, group name [", pGroupName,"] success [",(bAdded?"Y":"N"),"]");
		return bAdded;
	}

	__declspec(dllexport) const bool __cdecl readItemSync(const char* const pGroupName, const char* pItemPath, const int charBuffSz, char* valueOut, int& qualityOut, int& typeOut, char* timestampOut)
	{
		log_NOTICE("readItemSync+, group [",pGroupName,"] item [",pItemPath,"] buffSz [",(pantheios::integer)charBuffSz,"]");

		bool result = false;
		try
		{
			OPCItemData itemData;
			result = gsoGroupManager.ReadItemSync(pGroupName, pItemPath, itemData);

			// create itemValue with data if available, otherwise empty (which creates #NO_VALUE# entries).
			ItemValueStruct itemValue(result? &itemData: 0);

			// write to output values
			itemValue.duplicateTo(charBuffSz, valueOut, qualityOut, typeOut, timestampOut);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("readItemSync: [", gstrLastError,"]");
		}

		log_NOTICE("readItemSync-, result [",(result?"Y":"N"),"] group [",pGroupName,"] item [",pItemPath,"] value [",valueOut,"] quality [",(pantheios::integer)qualityOut,"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl readItemAsync(const char* const pGroupName, const char* pItemPath)
	{
		log_NOTICE("readItemAsync+, group [",pGroupName,"] item [",pItemPath,"]");

		bool result = false;
		try
		{
			result = gsoGroupManager.ReadItemAsync(pGroupName, pItemPath);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("readItemAsync: [", gstrLastError,"]");
		}

		log_NOTICE("readItemAsync-, success [", (result?"Y":"N"),"]");
		return false;
	}


	__declspec(dllexport) const bool __cdecl writeItemSync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		log_NOTICE("writeItemSync+, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"]");
		
		bool result = false;
		try
		{
			gsoGroupManager.WriteItemSync(pGroupName, pItemPath, pValue);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("writeItemSync: [", gstrLastError,"]");
		}

		log_NOTICE("writeItemSync-, group [", pGroupName,"] item [", pItemPath,"] success [", (result?"Y":"N"),"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl writeItemAsync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		log_NOTICE("writeItemAsync+, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"]");
		
		bool result = false;
		try
		{
			result = gsoGroupManager.WriteItemAsync(pGroupName, pItemPath, pValue);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("writeItemAsync: [", gstrLastError,"]");
		}
		
		log_NOTICE("writeItemAsync-, group [", pGroupName,"] item [", pItemPath,"] value [", pValue,"], success [", (result?"Y":"N"),"]");
		return result;
	}

	__declspec(dllexport) const bool __cdecl getItemNames(char* itemsBuffer[], const int nElementLength, const int nNumElements, const int nOffSet)
	{
		log_NOTICE("getItemNames+, nNumElements [",((pantheios::integer)nNumElements),"] nElementLength [",((pantheios::integer)nElementLength),"] nOffset [", ((pantheios::integer)nOffSet),"]");

		bool result = false;

		try
		{
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

			result = nNumElements > gsoOpcServerAddressSpace.GetCount() - nOffSet;
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("getItemNames: [", gstrLastError,"]");
		}

		log_NOTICE("getItemNames-, result [",(result?"Y":"N"),"]");
		
		return result;
	}

	__declspec(dllexport) void __cdecl getLastError(char* const pErrorBuffer, const int nBuffSz)
	{
		strcpy_s(pErrorBuffer, nBuffSz, gstrLastError.c_str());
	}

	__declspec(dllexport) void __cdecl registerAsyncUpdate(updateCallback cb)
	{
		log_NOTICE("registerAsyncUpdate+");

		try
		{
			updateHandler.SetCallback(cb);
		}
		catch(OPCException e)
		{
			gstrLastError = "OPC Exception: " + e.reasonString();
			log_ERROR("registerAsyncUpdate: [", gstrLastError,"]");
		}

		log_NOTICE("registerAsyncUpdate-");
	}
}