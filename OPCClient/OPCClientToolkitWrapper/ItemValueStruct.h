#pragma once

#include <atlbase.h>
#include <atlstr.h>
#include <atlexcept.h>
#include <atlcoll.h>
#include <objbase.h>
#include <COMCat.h>
#include <stdexcept>
#include "opcda.h"
#include "OPCItemData.h"


/**
* structure represents an outward bound (i.e. for the Java layers in the script runner controlling this dll)
* notion of an OPCItemData instance. The Java layer has a JNA.Structure based class which maps onto this 
* class. So if you add/remove anything to the struct, ensure the Java layer corresponds.
*
*/
struct ItemValue
{
	// item value
	char* value;

	// quality, 8 bit field - 11xxxxxx GOOD, 01xxxxxx UNCERTAIN, 00xxxxxx BAD
	int quality;

	// from value variant datatype
	int dataType;

	// format: YYYY/MM/DD-HH:MM:SS.mmm
	char* timestamp;
};

/**
* class is a wrapper (essentially for translation from OPCDataItem instances to ItemValue instances)
* and memory management.
*/
class ItemValueStruct
{
public:
	ItemValueStruct(const OPCItemData* itemData);
	virtual ~ItemValueStruct();

	const ItemValue& getItemValue(void) const;

private:
	ItemValue itemValue;

	void translateOPCItemData(const OPCItemData* itemData);
	std::string convertFILETIMEToString(const FILETIME& ft);
	void initialiseStructure(void);
};


void convertToItemValueStruct(const OPCItemData* const originalValue, ItemValue& itemValue, const int& charBuffSz);