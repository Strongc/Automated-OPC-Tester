#include "ItemValueStruct.h"
#include "Utils.h"
#include <string>
#include <sstream>
#include <iostream>

using namespace std;

const char* const NO_VALUE_STR = "#NO_VALUE#";

ItemValueStruct::ItemValueStruct(const OPCItemData* itemData)
{
	initialiseStructure();
	translateOPCItemData(itemData);
}

ItemValueStruct::~ItemValueStruct()
{
	// clear the memory to which the wrapped struct's char* members point to.
	if(itemValue.value != 0) free(itemValue.value);
	if(itemValue.timestamp != 0) free(itemValue.timestamp);
}

void ItemValueStruct::translateOPCItemData(const OPCItemData* itemData)
{
	// defaults
	string valueFieldBuffer = NO_VALUE_STR;
	string timestampFieldBuffer = NO_VALUE_STR;

	if(itemData != 0)
	{
		valueFieldBuffer = Utils::VariantToStringConverter(itemData->vDataValue);
		timestampFieldBuffer = convertFILETIMEToString(itemData->ftTimeStamp);

		itemValue.quality = itemData->wQuality;
		itemValue.dataType = itemData->vDataValue.vt;
	}

	// alloc cleared in ~dtor().
	itemValue.value = _strdup(valueFieldBuffer.c_str());
	itemValue.timestamp = _strdup(timestampFieldBuffer.c_str());
}

std::string ItemValueStruct::convertFILETIMEToString(const FILETIME& ft)
{
	SYSTEMTIME st;
	if(FileTimeToSystemTime(&ft, &st))
	{
		ostringstream oss;
		oss << st.wYear << "/" << st.wMonth << "/" << st.wDay << "-" << st.wHour << ":" << st.wMinute << ":" << st.wSecond << "." << st.wMilliseconds;
		return oss.str();
	}
	else
	{
		return "ERROR, failed to convert filetime to systemtime";
	}
}

const ItemValue& ItemValueStruct::getItemValue(void) const
{
	return itemValue;
}

void ItemValueStruct::initialiseStructure(void)
{
	itemValue.value = 0;
	itemValue.timestamp = 0;
	itemValue.quality = 0;
	itemValue.dataType = 0;
}

/**
* Note duplication assumes that the target instances char buffers (value and timestamp)
* have size charBuffSz. If not, trouble.
*/
void ItemValueStruct::duplicateTo(const int charBuffSz, char* valueOut, int& qualityOut, int& typeOut, char* timestampOut) const
{
	duplicateStringField(itemValue.value, valueOut, charBuffSz);
	duplicateStringField(itemValue.timestamp, timestampOut, charBuffSz);
	qualityOut = itemValue.quality;
	typeOut = itemValue.dataType;
}

void ItemValueStruct::duplicateStringField(const char* const srcStringField, char* dstStringField, const int charBuffSz) const
{
	if(srcStringField != 0)
	{
		_snprintf_s(dstStringField, charBuffSz, charBuffSz-1, "%s", srcStringField);
	}
	else
	{
		_snprintf_s(dstStringField, charBuffSz, charBuffSz-1, "%s", NO_VALUE_STR);
	}
}