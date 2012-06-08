#pragma once

#include "OPCClient.h"
#include <atlstr.h>

typedef int(*updateCallback)(const char* path, const char* value, const int quality, const int type, const char* timestamp);

class AsyncUpdateHandler : public IAsynchDataCallback
{
private:
	updateCallback callbackFn;

public:
	AsyncUpdateHandler(void);
	virtual ~AsyncUpdateHandler(void);

	void SetCallback(updateCallback cb);
	virtual void OnDataChange(COPCGroup & group, CAtlMap<COPCItem *, OPCItemData *> & changes);
};
