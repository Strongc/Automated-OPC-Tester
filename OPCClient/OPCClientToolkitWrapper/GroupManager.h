#pragma once

#include "OPCItem.h"
#include "Utils.h"
#include <assert.h>

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;

extern CAtlArray<CString> gsoOpcServerAddressSpace;

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
			const char* const pItemName = (pItem != NULL? pItem->getName(): "NULL");

			if(pItem != NULL)
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
			}

			// uh oh: no datatype property found
			assert(false);
			RecordError("GetDataTypePropertyDsc - failed to find OPC item data type property description for [%s]", pItemName);

			return auto_ptr<CPropertyDescription>(NULL);
		}

		int GetItemDataType(COPCItem* pItem)
		{
			const char* const pItemName = (pItem != NULL? pItem->getName(): "NULL");

			if(pItem != NULL)
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
			}

			// uh oh: no property descriptor found for data type property
			assert(false);
			RecordError("GetItemDataType - failed to find OPC item data type for [%s]", pItemName);
			return -1;
		}

		bool WriteItem(const char* const pItemName, const char* const pValue, const bool isAsync)
		{
			log_NOTICE("writing item [", pItemName,"]");
			COPCItem* pItem = m_items[CString(pItemName)];
			if(pItem != NULL)
			{
				log_NOTICE("got item [", pItemName,"]");
				VARTYPE vt = m_itemsDataTypes[CString(pItemName)];
				if(VT_EMPTY != vt)
				{
					log_NOTICE("got item type [", pantheios::integer(vt),"]");
					_variant_t varValue(pValue);
					if(S_OK == VariantChangeType(&varValue.GetVARIANT(), &varValue.GetVARIANT(), VARIANT_ALPHABOOL, vt))
					{
						if(isAsync)
						{
							log_NOTICE("starting async write");
							pItem->writeAsynch(varValue);
							log_NOTICE("completed async write");
						}
						else
						{
							pItem->writeSync(varValue);
						}
						
						log_NOTICE("WriteItem: successfully requested write item [", pItemName,"] value [", pValue,"] async [",(isAsync?"T":"F"),"]");
						return true;
					}
				}
			}

			RecordError("WriteItem: failed to write item [%s] value [%s] async [%s]", pItemName, pValue, (isAsync?"T":"F"));
			return false;
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

			RecordError("ReadItemSync: failed to write item [%s]", pItemName);
			return false;
		}

		bool WriteItemAsync(const char* const pItemName, const char* const pValue)
		{
			return WriteItem(pItemName, pValue, true);
		}

		bool WriteItemSync(const char* const pItemName, const char* const pValue)
		{
			return WriteItem(pItemName, pValue, false);
		}
	};

private:
	CAtlMap<CString , GroupNode*> m_groups;
	IAsynchDataCallback& m_updateHandler;

public:
	GroupManager(IAsynchDataCallback& updateHandler)
		:m_updateHandler(updateHandler)
	{
		m_groups.InitHashTable(257);
	};


	void AddGroup(const char* const pGroupName, COPCGroup* pGroup)
	{
		pGroup->enableAsynch(m_updateHandler);
		m_groups[CString(pGroupName)] = new GroupNode(pGroupName, pGroup);
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