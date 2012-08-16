#pragma once

#include "OPCClient.h"
#include <atlstr.h>
/*
typedef struct s_Update
{
	char* path;
	char* attributeId;
	char* value;
	int quality;
	int type;
	char* timestamp;
} Update;
*/
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

private:
	//Update* createUpdate(const char* path, const char* value, const int quality, const int type, const char* timestamp) const;
	//void destroyUpdate(Update* update) const;

};
