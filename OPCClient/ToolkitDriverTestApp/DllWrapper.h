#pragma once

#include "stdafx.h"
#include <windows.h>

class DllWrapper
{
public:
	DllWrapper(HINSTANCE hDllHandle);
	virtual ~DllWrapper(void);

	void init(const char* const pHost, const char* const pServer);
	void end();
	void createGroup(const char* const pGroupName, const unsigned long requestedRefreshRate);
	void destroyGroup(const char* const pGroupName);
	void addItem(const char* const pGroupName, const char* pItemPath);
	void getItemNames();

private:
	HINSTANCE m_hDllHandle;
	FARPROC GetDllMethod(const char* const cMethodName);
};
