#pragma once

#include "OPCClient.h"
#include <atlstr.h>

typedef struct s_Update
{
	char* path;
	char* attributeId;
	char* value;
	int quality;
	int type;
	char* timestamp;
} Update;

typedef int(*updateCallback)(Update* update);

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
