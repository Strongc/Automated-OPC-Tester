#include "stdafx.h"
#include "gtest\gtest.h"

#include "OPCHost.h"

extern "C" const char PANTHEIOS_FE_PROCESS_IDENTITY[] = "OPC_CLIENT_DLL_TESTS";

// test instance - normally instantiated by dllInterface and referenced elsewhere as an extern.
// avoids having to drag in dllInterface.cpp (and all the attendant compilation problems)
CString gstrLastError = "No errors reported";

int _tmain(int argc, _TCHAR* argv[])
{
	::testing::InitGoogleTest(&argc, argv);
	return RUN_ALL_TESTS();
}

