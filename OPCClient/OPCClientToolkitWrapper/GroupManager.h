#pragma once

#include "OPCItem.h"
#include "Utils.h"

#include <pantheios/pantheios.hpp>

using namespace pantheios;

class GroupManager
{
	struct GroupNode
	{

	private:
		char* m_pGroupName;
		COPCGroup* m_pGroup;
		CAtlMap<CString , COPCItem *> m_items;

	public:
		GroupNode(const char* const pGroupName, COPCGroup* pGroup):m_pGroupName(_strdup(pGroupName)), m_pGroup(pGroup)
		{
			m_items.InitHashTable(257);
		};

		void AddItem(const char* const pItemName)
		{
			log_NOTICE("GroupNode [", m_pGroupName,"] adding item [", pItemName,"]");
			COPCItem* pItem = m_pGroup->addItem(CString(pItemName), true);
			m_items[CString(pItemName)] = pItem;
		};

		bool ReadItemSync(const char* const pItemName, char* pBuff, size_t szBuff)
		{
			log_NOTICE("ReadItemSync: GroupNode [", m_pGroupName,"] reading item [", pItemName,"]");
			memset(pBuff, 0, szBuff);

			COPCItem* pItem = m_items[CString(pItemName)];
			if(pItem != NULL)
			{
				log_NOTICE("ReadItemSync: GroupNode [", m_pGroupName,"] found item [", pItemName,"]");
	  			OPCItemData data;
	  			pItem->readSync(data, OPC_DS_DEVICE);

				log_NOTICE("ReadItemSync: GroupNode [", m_pGroupName,"] translating item [", pItemName,"]");
				ConvertOPCItemDataValueToCharArray(data, pBuff, szBuff);
				return true;
			}

			return false;
		}
	};

private:
	CAtlMap<CString , GroupNode*> m_groups;

public:
	GroupManager()
	{
		m_groups.InitHashTable(257);
	};


	void AddGroup(const char* const pGroupName, COPCGroup* pGroup)
	{
		m_groups[CString(pGroupName)] = new GroupNode(pGroupName, pGroup);
	};

	bool AddItem(const char* pGroupName, const char* pItemAddress)
	{
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->AddItem(pItemAddress);

			return true;
		}
		else
		{
			return false;
		}
	};

	bool ReadItemSync(const char* const pGroupName, const char* const pItemAddress, char* pBuff, size_t szBuff)
	{
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->ReadItemSync(pItemAddress, pBuff, szBuff);

			log_NOTICE("readItemSync: group [", pGroupName,"] item [",pItemAddress,"] value [",pBuff,"]");
			return true;
		}
		else
		{
			return false;
		}
	};
};
