#pragma once

#include "OPCItem.h"
#include "Utils.h"
#include <assert.h>

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;

class GroupManager
{
	struct GroupNode
	{

	private:
		char* m_pGroupName;
		COPCGroup* m_pGroup;
		CAtlMap<CString , COPCItem *> m_items;
		CAtlMap<CString, int> m_itemsDataTypes;

		auto_ptr<CPropertyDescription> GetDataTypePropertyDsc(COPCItem* pItem)
		{
			CAtlArray<CPropertyDescription> itemProperties;
			pItem->getSupportedProperties(itemProperties);

			for(unsigned int i = 0; i < itemProperties.GetCount(); i++)
			{
				CPropertyDescription& props = itemProperties.GetAt(i);

				if(string(props.desc).find("DataType") != string::npos)
				{
					return auto_ptr<CPropertyDescription>(new CPropertyDescription(props.id, props.desc, props.type));
				}
			}

			// uh oh: no datatype property found
			assert(false);
			return auto_ptr<CPropertyDescription>(NULL);
		}

		int GetItemDataType(COPCItem* pItem)
		{
			auto_ptr<CPropertyDescription> propDsc = GetDataTypePropertyDsc(pItem);
			if(propDsc.get() != NULL)
			{
				log_NOTICE("Found DataType property description for item [", pItem->getName(),"]");

				CAtlArray<CPropertyDescription> propsToRead;
				propsToRead.Add(*propDsc.get());

				ATL::CAutoPtrArray<SPropertyValue> propValues;
				pItem->getProperties(propsToRead, propValues);

				assert(1 == propValues.GetCount());

				_variant_t value(propValues[0]->value);
				return static_cast<int>(value);
			}
			else
			{
				// uh oh: no property descriptor found for data type property
				assert(false);
				return -1;
			}
		}

	public:

		GroupNode(const char* const pGroupName, COPCGroup* pGroup):m_pGroupName(_strdup(pGroupName)), m_pGroup(pGroup)
		{
			m_items.InitHashTable(257);
			m_itemsDataTypes.InitHashTable(257);
		};

		void AddItem(const char* const pItemName)
		{
			log_NOTICE("GroupNode [", m_pGroupName,"] adding item [", pItemName,"]...");
			COPCItem* pItem = m_pGroup->addItem(CString(pItemName), true);
						
			m_items[CString(pItemName)] = pItem;
			m_itemsDataTypes[CString(pItemName)] = GetItemDataType(pItem);

			log_NOTICE("Added item to group - name [", pItemName,"] type [", pantheios::integer(m_itemsDataTypes[CString(pItemName)]),"]");
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

		bool WriteItemSync(const char* const pItemName, const char* const pValue)
		{
			COPCItem* pItem = m_items[CString(pItemName)];
			if(pItem != NULL)
			{
				VARTYPE vt = m_itemsDataTypes[CString(pItemName)];
				if(VT_EMPTY != vt)
				{
					_variant_t varValue(pValue);
					if(S_OK == VariantChangeType(&varValue.GetVARIANT(), &varValue.GetVARIANT(), VARIANT_ALPHABOOL, vt))
					{
						pItem->writeSync(varValue);
						log_NOTICE("WriteItemSync: successfully wrote item [", pItemName,"] value [", pValue,"]");
						return true;
					}
				}
			}

			log_ERROR("WriteItemSync: failed to write item [", pItemName,"] value [", pValue,"]");
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

	bool ReadItemSync(const char* const pGroupName, const char* const pItemPath, char* pBuff, size_t szBuff)
	{
		GroupNode* pGroupNode = m_groups[CString(pGroupName)];
		if(pGroupNode != NULL)
		{
			pGroupNode->ReadItemSync(pItemPath, pBuff, szBuff);

			log_NOTICE("readItemSync: group [", pGroupName,"] item [",pItemPath,"] value [",pBuff,"]");
			return true;
		}
		else
		{
			return false;
		}
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
		else
		{
			return false;
		}
	}
};