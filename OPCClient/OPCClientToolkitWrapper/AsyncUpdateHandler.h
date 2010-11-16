#pragma once

#include "OPCClient.h"

class AsyncUpdateHandler : public IAsynchDataCallback
{
public:
	AsyncUpdateHandler(void);
	virtual ~AsyncUpdateHandler(void);

	virtual void OnDataChange(COPCGroup & group, CAtlMap<COPCItem *, OPCItemData *> & changes);
};
