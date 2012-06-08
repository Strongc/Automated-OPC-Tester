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

		log_NOTICE("\t item [",pPair->m_key->getName(),"] value [", itemValueStruct.getItemValue().value,"]");
		if(callbackFn != NULL)
		{
			log_NOTICE("\t OnDataChange calling callback fn");
			const ItemValue& itemValue = itemValueStruct.getItemValue();
			callbackFn(pPair->m_key->getName(), itemValue.value, itemValue.quality, itemValue.dataType, itemValue.timestamp);
			log_DEBUG("\t OnDataChange called callback fn");
		}
	}
}

void AsyncUpdateHandler::SetCallback(updateCallback cb)
{
	log_NOTICE("Setting callbackFn - is currently NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
	this->callbackFn = cb;
	log_NOTICE("Setting callbackFn - is now NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
}
