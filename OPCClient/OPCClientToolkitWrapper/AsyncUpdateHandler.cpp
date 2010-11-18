#pragma once

#include "AsyncUpdateHandler.h"
#include "OPCGroup.h"
#include "Utils.h"
#include "OPCItem.h"

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
	log_NOTICE("OnDataChange called, group [", group.getName(),"] change count [", pantheios::integer(changes.GetCount()),"]");
	for(POSITION pos = changes.GetStartPosition(); pos != NULL; )
	{
		CAtlMap<COPCItem *, OPCItemData *>::CPair* pPair = changes.GetNext(pos);

		char buff[nMaxBuffSz];
		memset(buff, nMaxBuffSz, 0);
		ConvertOPCItemDataValueToCharArray(*pPair->m_value, buff, nMaxBuffSz);

		log_NOTICE("\t item [",pPair->m_key->getName(),"] value [", buff,"]");
	}

	if(callbackFn != NULL)
	{
		log_NOTICE("OnDataChange calling callback fn");
		this->callbackFn("woo", "hoo");
		log_NOTICE("OnDataChange called callback fn");
	}
	else
	{
		log_NOTICE("OnDataChange no callback registered");
	}
}

void AsyncUpdateHandler::SetCallback(updateCallback cb)
{
	log_NOTICE("Setting callbackFn - is currently NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
	this->callbackFn = cb;
	cb("woo", "hoo");
	log_NOTICE("Setting callbackFn - is now NULL [", (this->callbackFn == NULL?"Y":"N"),"]");
}
