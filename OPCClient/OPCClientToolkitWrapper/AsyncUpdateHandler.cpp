#pragma once

#include "AsyncUpdateHandler.h"
#include "OPCGroup.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;


AsyncUpdateHandler::AsyncUpdateHandler(void)
{
}

AsyncUpdateHandler::~AsyncUpdateHandler(void)
{
}

void AsyncUpdateHandler::OnDataChange(COPCGroup & group, CAtlMap<COPCItem *, OPCItemData *> & changes)
{
	log_NOTICE("OnDataChange called, group [", group.getName(),"] change count [", pantheios::integer(changes.GetCount()),"]");
}
