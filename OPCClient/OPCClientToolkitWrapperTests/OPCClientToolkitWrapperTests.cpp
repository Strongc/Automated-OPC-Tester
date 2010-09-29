#include "stdafx.h"
#include "gtest\gtest.h"

extern "C" const char PANTHEIOS_FE_PROCESS_IDENTITY[] = "OPC_CLIENT_DLL_TESTS";

int _tmain(int argc, _TCHAR* argv[])
{
	::testing::InitGoogleTest(&argc, argv);
	return RUN_ALL_TESTS();
}

