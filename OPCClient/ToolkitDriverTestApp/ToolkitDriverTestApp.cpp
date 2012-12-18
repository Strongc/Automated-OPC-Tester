// ToolkitDriverTestApp.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "DllWrapper.h"
#include <iostream>
#include <sstream>


using namespace std;

const LPCTSTR gsDllName = _T("AutomatedOpcTester.dll");
const char* const gsOPCServerName = "Matrikon.OPC.Simulation";
/*
int updateCallbackFn(Update* update)
{
	cout << "path ["<<update->path<<"] value ["<<update->value<<"] quality ["<<update->quality<<"] type ["<<update->type<<"] timestamp ["<<update->timestamp<<"]" << endl;
	return 0;
}
*/
int updateCallbackFn (const char* path, const char* value, const int quality, const int type, const char* timestamp)
{
	cout << "path ["<<path<<"] value ["<<value<<"] quality ["<<quality<<"] type ["<<type<<"] timestamp ["<<timestamp<<"]" << endl;
	return 0;
}

const std::string accessRights(DllWrapper& dllWrapper, const string groupNm, const string itemPath)
{
	std::ostringstream result;

	DWORD accessRights = dllWrapper.getItemAccessRights(groupNm.c_str(), itemPath.c_str());
	
	result << "group ["<<groupNm<<"] item ["<<itemPath<<"] access rights ["<<accessRights<<"]";

	return result.str();
}

int _tmain(int argc, _TCHAR* argv[])
{
	wcout << "loading " << gsDllName << "..." << endl;
	DllWrapper dllWrapper(LoadLibrary(gsDllName));
	
	dllWrapper.init("", gsOPCServerName);
	dllWrapper.getItemNames();

	string baseGroupNm("testGroup");
	for(int i=0; i<1; i++)
	{
		char buff[8];
		_itoa_s(i, buff, 10);

		string groupNm(baseGroupNm);
		groupNm.append(buff);

		cout << "creating group ["<<groupNm.c_str()<<"]" << endl;
		dllWrapper.createGroup(groupNm.c_str(), 10);
		
		Sleep(750);

		dllWrapper.registerAsyncUpdate(updateCallbackFn);

		dllWrapper.addItem(groupNm.c_str(), "testGroup.myBigFloat");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.myBool");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.myLongInt");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.myReadOnly");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.myShortInt");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.mySmallFloat");
		dllWrapper.addItem(groupNm.c_str(), "testGroup.myString");
		cout << "added items" << endl;
		
		cout << accessRights(dllWrapper, groupNm, "testGroup.myBigFloat") << endl;
		cout << accessRights(dllWrapper, groupNm, "I.do.not.exist") << endl;
		
		Sleep(120000);

		dllWrapper.destroyGroup(groupNm.c_str());
		cout << "destroyed group" << endl;
	}

	dllWrapper.end();

	return 0;
}



