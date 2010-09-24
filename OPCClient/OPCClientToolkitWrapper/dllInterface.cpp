#include "common.h"
#include <iostream>
#include <time.h>
#include "comutil.h"

#include "OPCClient.h"
#include "OPCHost.h"
#include "OPCServer.h"
#include "OPCGroup.h"
#include "GroupManager.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/backends/bec.file.h>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;

bool gsbLoggingInitialised = false;
char gscLogFilePath[MAX_PATH];

COPCHost *gspHost = NULL;
COPCServer *gspOpcServer = NULL;
GroupManager gsoGroupManager;


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
}

extern "C"
{
	__declspec(dllexport) void __cdecl init(const char* const pHost, const char* const pServer)
	{
		cout << "Initialising with host ["<< pHost <<"] port ["<< pServer <<"]" << endl;

		InitialiseLogging();

		COPCClient::init();
		log_NOTICE("init - intialised OPCClient class");

		gspHost = COPCClient::makeHost(pHost);
		log_NOTICE("init: made host [", pHost,"]");

		gspOpcServer = gspHost->connectDAServer(pServer);
		log_NOTICE("init: connected opcServer [", pServer,"]");
	}

	__declspec(dllexport) const unsigned long __cdecl createGroup(const char* const pGroupName, const unsigned long requestedRefreshRate)
	{
		log_NOTICE("createGroup called, group name [", pGroupName,"] requested refresh rate [", ((pantheios::integer)requestedRefreshRate),"]");

		unsigned long actualRefreshRate = requestedRefreshRate;
		COPCGroup *pGroup = gspOpcServer->makeGroup(pGroupName, true, requestedRefreshRate, actualRefreshRate, 0.0);
		gsoGroupManager.AddGroup(pGroupName, pGroup);

		log_NOTICE("createGroup completed, for group name [", pGroupName,"]");
		return actualRefreshRate;
	} 

	__declspec(dllexport) const bool __cdecl addItem(const char* const pGroupName, const char* pItemPath)
	{
		log_NOTICE("addItem called, group name [", pGroupName,"] item name [", pItemPath,"]");

		bool bAdded = gsoGroupManager.AddItem(pGroupName, pItemPath);
		log_NOTICE("addItem, success [",(bAdded?"Y":"N"),"]");

		log_NOTICE("addItem completed, for group name [", pGroupName,"]");
		return true;
	}

	__declspec(dllexport) const bool __cdecl readItemSync(const char* const pGroupName, const char* pItemPath, char* pBuff, const int nBuffSz)
	{
		log_NOTICE("readItemSync called, group [",pGroupName,"] item [",pItemPath,"] initial buffer [", pBuff,"] buffer sz [", pantheios::integer(nBuffSz),"]");
		bool result = gsoGroupManager.ReadItemSync(pGroupName, pItemPath, pBuff, nBuffSz);

		log_NOTICE("readItemSync complete, buffer [", pBuff,"] buffer sz [", pantheios::integer(nBuffSz),"]");
		return result;
	}

	__declspec(dllexport) void __cdecl getItemNames(void)
	{
		log_NOTICE("getItemNames: called");

		CAtlArray<CString> opcItemNames ;
	  	gspOpcServer->getItemNames(opcItemNames);

		log_NOTICE("getItemNames: there are [", pantheios::integer(opcItemNames.GetCount()),"] items");

		for(unsigned int i=0; i<opcItemNames.GetCount(); i++)
		{
			char itemName[100];
			strcpy_s(itemName, 100, CStringA(opcItemNames[i]).GetString());
			log_NOTICE("getItemNames: string [", pantheios::integer(i),"] is [", itemName,"]");
		}
	}
}