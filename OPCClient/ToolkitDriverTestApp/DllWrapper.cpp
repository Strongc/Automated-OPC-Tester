#include "stdafx.h"
#include <windows.h>
#include <iostream>
#include "DllWrapper.h"

using namespace std;

typedef void (__cdecl *DLL_INIT_EXPORTED_METHOD)(const char* const, const char* const);
typedef void (__cdecl *DLL_END_EXPORTED_METHOD)();
typedef unsigned long (__cdecl *DLL_CREATE_GROUP_EXPORTED_METHOD)(const char* const pGroupName, const unsigned long requestedRefreshRate);
typedef const bool (__cdecl *DLL_DESTROY_GROUP_EXPORTED_METHOD)(const char* const pGroupName);
typedef const bool (__cdecl *DLL_ADD_ITEM_EXPORTED_METHOD)(const char* const pGroupName, const char* pItemPath);
typedef const bool (__cdecl *DLL_GET_ITEM_NAMES_EXPORTED_METHOD)(char* itemsBuffer[], const int nElementLength, const int nNumElements, const int nOffSet);
typedef const void (__cdecl *DLL_REGISTER_ASYNC_UPDATE_METHOD)(updateCallback cb);
typedef DWORD (__cdecl *DLL_GET_ITEM_ACCESS_RIGHTS_EXPORTED_METHOD)(const char* const pGroupName, const char* const pItemPath);

DllWrapper::DllWrapper(HINSTANCE hDllHandle)
:m_hDllHandle(hDllHandle)
{
	if(m_hDllHandle == NULL)
		throw "Failed to load dll";
	else
		cout << "Dll loaded OK" << endl;
}

DllWrapper::~DllWrapper(void)
{
	FreeLibrary(m_hDllHandle);
}

FARPROC DllWrapper::GetDllMethod(const char* const cMethodName)
{
	return GetProcAddress(m_hDllHandle, cMethodName);
}

void DllWrapper::init(const char* const pHost, const char* const pServer)
{
	cout << "DllWrapper::init called for host ["<<pHost<<"] server ["<<pServer<<"]" << endl;
	
	DLL_INIT_EXPORTED_METHOD dllInitMethod = (DLL_INIT_EXPORTED_METHOD)GetDllMethod("init");
	dllInitMethod(pHost, pServer);

	cout << "DllWrapper::init complete" << endl;
}

void DllWrapper::end()
{
	cout << "DllWrapper::end called" << endl;

	DLL_END_EXPORTED_METHOD dllEndMethod = (DLL_END_EXPORTED_METHOD)GetDllMethod("end");
	dllEndMethod();

	cout << "DllWrapper::end complete" << endl;
}

void DllWrapper::createGroup(const char* const pGroupName, const unsigned long requestedRefreshRate)
{
	cout << "DllWrapper::createGroup called name ["<<pGroupName<<"] refresh rate ["<<requestedRefreshRate<<"]" << endl;

	DLL_CREATE_GROUP_EXPORTED_METHOD dllCreateGroupMethod = (DLL_CREATE_GROUP_EXPORTED_METHOD)GetDllMethod("createGroup");
	dllCreateGroupMethod(pGroupName, requestedRefreshRate);

	cout << "DllWrapper::createGroup complete" << endl;
}

void DllWrapper::destroyGroup(const char* const pGroupName)
{
	cout << "DllWrapper::destroyGroup called name ["<<pGroupName<<"]" << endl;

	DLL_DESTROY_GROUP_EXPORTED_METHOD dllDestroyGroupMethod = (DLL_DESTROY_GROUP_EXPORTED_METHOD)GetDllMethod("destroyGroup");
	dllDestroyGroupMethod(pGroupName);

	cout << "DllWrapper::destroyGroup completed" << endl;
}

void DllWrapper::addItem(const char* const pGroupName, const char* pItemPath)
{
	cout << "DllWrapper::addItem item ["<<pItemPath<<"] group ["<<pGroupName<<"]" << endl;

	DLL_ADD_ITEM_EXPORTED_METHOD dllAddItemMethod = (DLL_ADD_ITEM_EXPORTED_METHOD)GetDllMethod("addItem");
	dllAddItemMethod(pGroupName, pItemPath);

	cout << "DllWrapper::addItem completed" << endl;
}

void DllWrapper::getItemNames()
{
	cout << "DllWrapper::getItemNames called" << endl;

	DLL_GET_ITEM_NAMES_EXPORTED_METHOD dllGetItemNames = (DLL_GET_ITEM_NAMES_EXPORTED_METHOD)GetDllMethod("getItemNames");
	
	char *buff[300];
	for(int i=0; i<300; i++)
	{
		buff[i] = new char[300];
	}
	//char buff[300][300];
	dllGetItemNames(buff, 300, 300, 0);
	//char* itemsBuffer[], const int nElementLength, const int nNumElements, const int nOffSet

	for(int i=0; i<300; i++)
	{
		char *pBuff = buff[i];
		delete[] pBuff;
		buff[i] = NULL;
	}

	cout << "DllWrapper::getItemNames complete" << endl;
}

void DllWrapper::registerAsyncUpdate(updateCallback cb)
{
	cout << "DllWrapper::registerAsyncUpdate+" << endl;

	DLL_REGISTER_ASYNC_UPDATE_METHOD dllRegisterAsyncUpdate = (DLL_REGISTER_ASYNC_UPDATE_METHOD)GetDllMethod("registerAsyncUpdate");
	dllRegisterAsyncUpdate(cb);

	cout << "DllWrapper::registerAsyncUpdate-" << endl;
}

DWORD DllWrapper::getItemAccessRights(const char* const pGroupName, const char* const pItemPath)
{
	cout << "DllWrapper::getItemAccessRights called group ["<<pGroupName<<"] item ["<<pItemPath<<"]" << endl;

	DLL_GET_ITEM_ACCESS_RIGHTS_EXPORTED_METHOD dllGetItemAccessRightsMethod = (DLL_GET_ITEM_ACCESS_RIGHTS_EXPORTED_METHOD)GetDllMethod("getItemAccessRights");
	const DWORD accessRights = dllGetItemAccessRightsMethod(pGroupName, pItemPath);

	cout << "DllWrapper::getItemAccessRights complete" << endl;
	return accessRights;
}
