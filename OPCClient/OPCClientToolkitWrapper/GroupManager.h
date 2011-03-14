#pragma once

#include "OPCItem.h"
#include "Utils.h"
#include <assert.h>
#include "TransactionCompleteHandler.h"
#include "GroupNode.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;

extern COPCServer *gspOpcServer;
extern CAtlArray<CString> gsoOpcServerAddressSpace;
extern TransactionCompleteHandler transactionHandler;

class GroupManager
{
	

private:
	CAtlMap<CString , GroupNode*> m_groups;
	IAsynchDataCallback& m_updateHandler;

public:
	GroupManager(IAsynchDataCallback& updateHandler)
		:m_updateHandler(updateHandler)
	{
		m_groups.InitHashTable(257);
	};


	unsigned long CreateGroup(const char* const pGroupName, const unsigned long requestedRefreshRate)
	{
		if(m_groups.Lookup(pGroupName))
		{
			log_WARNING("CreateGroup called with already existent group [", pGroupName,"]");
			return requestedRefreshRate;
		}

		unsigned long actualRefreshRate = requestedRefreshRate;
		COPCGroup *pGroup = gspOpcServer->makeGroup(pGroupName, /*true*/false, requestedRefreshRate, actualRefreshRate, 0.0);
		
		pGroup->enableAsynch(m_updateHandler);
		m_groups[CString(pGroupName)] = new GroupNode(pGroupName, pGroup);

		return actualRefreshRate;
	};

	bool DestroyGroup(const char* const pGroupName)
	{
		if(!m_groups.Lookup(pGroupName))
		{
			log_WARNING("DestroyGroup called with non-existent group [", pGroupName,"]");
			return false;
		}

		GroupNode* pNode = m_groups[CString(pGroupName)];

		if(!m_groups.RemoveKey(pGroupName))
		{			
			log_ERROR("DestroyGroup failed to remove key [", pGroupName,"] item count [", ((pantheios::integer)m_groups.GetCount()),"]");
			assert(false);

		}
		delete pNode;

		return true;
	};

	bool AddItem(const char* pGroupName, const char* pItemAddress)
	{
		if(!IsValidItem(pItemAddress)) return false;

		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->AddItem(pItemAddress);
			return true;
		}

		RecordError("AddItem: failed to find group [%s]", pGroupName);
		return false;
	};

	bool ReadItemSync(const char* const pGroupName, const char* const pItemPath, char* pBuff, size_t szBuff)
	{
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->ReadItemSync(pItemPath, pBuff, szBuff);

			log_NOTICE("readItemSync: group [", pGroupName,"] item [",pItemPath,"] value [",pBuff,"]");
			return true;
		}

		RecordError("ReadItemSync: failed to find group [%s]", pGroupName);
		return false;
	};


	bool WriteItemSync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->WriteItemSync(pItemPath, pValue);

			log_NOTICE("WriteItemSync: group [", pGroupName,"] item [",pItemPath,"] value [",pValue,"]");
			return true;
		}

		RecordError("WriteItemSync: failed to find group [%s]", pGroupName);
		return false;
	}

	bool ReadItemAsync(const char* const pGroupName, const char* const pItemPath)
	{
		log_NOTICE("ReadItemAsync: called with group [", pGroupName,"] item [",pItemPath,"] called");
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->ReadItemAsync(pItemPath);

			log_NOTICE("ReadItemAsync: called successfully for group [", pGroupName,"] item [",pItemPath,"]");
			return true;
		}

		RecordError("ReadItemAsync: failed to find group [%s]", pGroupName);
		return false;
	};

	bool WriteItemAsync(const char* const pGroupName, const char* pItemPath, const char* const pValue)
	{
		log_NOTICE("WriteItemAsync: called");
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			log_NOTICE("WriteItemAsync: found group, writing...");
			pGroupNode->WriteItemAsync(pItemPath, pValue);

			log_NOTICE("WriteItemAsync: group [", pGroupName,"] item [",pItemPath,"] value [",pValue,"]");
			return true;
		}

		RecordError("WriteItemAsync: failed to find group [%s]", pGroupName);
		return false;
	}

	bool IsValidItem(const char* const pItemPath)
	{
		unsigned int nAddressSpaceSz = gsoOpcServerAddressSpace.GetCount();

		for(unsigned int i=0; i<nAddressSpaceSz; i++)
		{
			if(strcmp(pItemPath, CStringA(gsoOpcServerAddressSpace[i]).GetString()) == 0)
			{
				return true;
			}
		}

		RecordError("IsValidItem [%s] was not found in opc server address space of size [%d]", pItemPath, nAddressSpaceSz);
		return false;
	}
};