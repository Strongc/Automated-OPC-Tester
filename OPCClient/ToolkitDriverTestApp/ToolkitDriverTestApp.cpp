// ToolkitDriverTestApp.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "DllWrapper.h"
#include <iostream>


using namespace std;

const LPCTSTR gsDllName = _T("AutomatedOpcTester.dll");

int _tmain(int argc, _TCHAR* argv[])
{
	wcout << "loading " << gsDllName << "..." << endl;
	DllWrapper dllWrapper(LoadLibrary(gsDllName));
	
	dllWrapper.init("", "WIENER.Plein.Baus.OPC.Server.DA");
	dllWrapper.getItemNames();

	string baseGroupNm("testGroup");
	for(int i=0; i<1000; i++)
	{
		char buff[8];
		_itoa_s(i, buff, 10);

		string groupNm(baseGroupNm);
		groupNm.append(buff);

		cout << "creating group ["<<groupNm.c_str()<<"]" << endl;
		dllWrapper.createGroup(groupNm.c_str(), 1000);
		
		Sleep(50);

		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.I1_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.I2_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.I4_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.UI1_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.UI2_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.UI4_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.R4_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.R8_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.BOOL_Cache");
		dllWrapper.addItem(groupNm.c_str(), "Server.TestObjects.BSTR_Cache");

		Sleep(50);

		dllWrapper.destroyGroup(groupNm.c_str());
	}

	dllWrapper.end();

	return 0;
}



