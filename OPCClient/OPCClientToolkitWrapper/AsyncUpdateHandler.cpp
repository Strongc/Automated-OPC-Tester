#pragma once

#include "AsyncUpdateHandler.h"
#include "OPCGroup.h"
#include "Utils.h"
#include "OPCItem.h"
#include "ItemValueStruct.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;

static const int nMaxBuffSz = 2000;
const char* const gsAttributeId = "UNUSED";


AsyncUpdateHandler::AsyncUpdateHandler(void)
:callbackFn(NULL)
{
}

AsyncUpdateHandler::~AsyncUpdateHandler(void)
{
}

void AsyncUpdateHandler::OnDataChange(COPCGroup & group, CAtlMap<COPCItem *, OPCItemData *> & changes)
{
	log_NOTICE("OnDataChange called, group [", group.getName(),"] change count [", pantheios::integer(changes.GetCount()),"] have callback [",(callbackFn != NULL?"Y":"N"),"]");
	
	for(POSITION pos = changes.GetStartPosition(); pos != NULL; )
	{
		CAtlMap<COPCItem *, OPCItemData *>::CPair* pPair = changes.GetNext(pos);

		ItemValueStruct itemValueStruct(pPair->m_value);		
		const ItemValue& itemValue = itemValueStruct.getItemValue();
		
		log_NOTICE("\t item [",pPair->m_key->getName(),"] value [", itemValueStruct.getItemValue().value,"]");

		if(callbackFn != NULL)
		{
			log_NOTICE("\t OnDataChange calling callback fn");			
			//Update* update = createUpdate(pPair->m_key->getName().GetString(), itemValue.value, itemValue.quality, itemValue.dataType, itemValue.timestamp);
			callbackFn(pPair->m_key->getName().GetString(), itemValue.value, itemValue.quality, itemValue.dataType, itemValue.timestamp);

			log_DEBUG("\t OnDataChange called callback fn");
/*			
			callbackFn(update);
			log_DEBUG("\t OnDataChange called callback fn");

			destroyUpdate(update);
			log_DEBUG("\t OnDataChange destroyed object");
*/
		}
	}
}
/*
Update* AsyncUpdateHandler::createUpdate(const char* path, const char* value, const int quality, const int type, const char* timestamp) const
{
	Update* update = new Update();

	update->path = _strdup(path);
	update->value = _strdup(value);
	update->quality = quality;
	update->type = type;
	update->timestamp = _strdup(timestamp);
	update->attributeId = _strdup(gsAttributeId);

	return update;
}

void AsyncUpdateHandler::destroyUpdate(Update* update) const
{
	free(update->path);
	free(update->value);
	free(update->timestamp);
	free(update->attributeId);
	delete update;
}
*/
void AsyncUpdateHandler::SetCallback(updateCallback cb)
{
	log_NOTICE("Setting callbackFn - is currently NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
	this->callbackFn = cb;
	log_NOTICE("Setting callbackFn - is now NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
}
