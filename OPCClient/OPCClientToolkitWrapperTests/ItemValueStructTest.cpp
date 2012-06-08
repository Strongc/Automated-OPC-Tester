#include "common.h"
#include <iostream>
#include <sstream>
#include <time.h>
#include "comutil.h"

#include "OPCClient.h"
#include "OPCHost.h"
#include "OPCServer.h"
#include "OPCGroup.h"

#include "ItemValueStruct.h"
#include "gtest\gtest.h"

using namespace std;

string getCurrentTime(FILETIME& ft)
{
    SYSTEMTIME st;
    GetSystemTime(&st);

    SystemTimeToFileTime(&st, &ft);  // Converts the current system time to file time format

	// format the time for return value
	ostringstream oss;
	oss << st.wYear << "/" << st.wMonth << "/" << st.wDay << "-" << st.wHour << ":" << st.wMinute << ":" << st.wSecond << "." << st.wMilliseconds;
	return oss.str();
}

TEST(ItemValueStructTest, testValueIsConvertedCorrectly)
{
	FILETIME now;
	string expectedTimestamp = getCurrentTime(now);

	OPCItemData data;
	data.vDataValue = _variant_t(-1234.5678);
	data.wQuality = 0x03; // first 2 bits ON = item quality GOOD.
	data.ftTimeStamp.dwHighDateTime = now.dwHighDateTime;
	data.ftTimeStamp.dwLowDateTime = now.dwLowDateTime;

	ItemValueStruct testee(&data);
	
	cout << "value is ["<<testee.getItemValue().value<<"]" << endl;
	cout << "timestamp is ["<<testee.getItemValue().timestamp<<"]" << endl;
	cout << "type is ["<<testee.getItemValue().dataType<<"]" << endl;
	
	ASSERT_EQ(0, string("-1234.5678").compare(testee.getItemValue().value));
	ASSERT_EQ(3, testee.getItemValue().quality);
	ASSERT_EQ(0, expectedTimestamp.compare(testee.getItemValue().timestamp));
	ASSERT_EQ(VT_R8, testee.getItemValue().dataType);
}

TEST(ItemValueStructTest, testNullDataItemPointerHandled)
{
	ItemValueStruct testee(0);

	ASSERT_EQ(0, string("#NO_VALUE#").compare(testee.getItemValue().value));
	ASSERT_EQ(0, testee.getItemValue().quality);
	ASSERT_EQ(0, string("#NO_VALUE#").compare(testee.getItemValue().timestamp));	
	ASSERT_EQ(VT_EMPTY, testee.getItemValue().dataType);
}

TEST(ItemValueStructTest, testBooleanTypeRecognised)
{
	OPCItemData data;
	data.vDataValue = _variant_t(true);

	ASSERT_EQ(VT_BOOL, ItemValueStruct(&data).getItemValue().dataType);
}

TEST(ItemValueStructTest, testIntegerTypeRecognised)
{
	OPCItemData data;
	data.vDataValue = _variant_t(INT_MAX);

	ASSERT_EQ(VT_INT, ItemValueStruct(&data).getItemValue().dataType);
}

TEST(ItemValueStructTest, testStringTypeRecognised)
{
	OPCItemData data;
	data.vDataValue = _variant_t("woohoo, yeah!");

	ASSERT_EQ(VT_BSTR, ItemValueStruct(&data).getItemValue().dataType);
}

TEST(ItemValueStructTest, testDuplication)
{
	FILETIME now;
	string expectedTimestamp = getCurrentTime(now);

	OPCItemData data;
	data.vDataValue = _variant_t(-1234.5678);
	data.wQuality = 0x03; // first 2 bits ON = item quality GOOD.
	data.ftTimeStamp.dwHighDateTime = now.dwHighDateTime;
	data.ftTimeStamp.dwLowDateTime = now.dwLowDateTime;

	ItemValueStruct itemValueWrapper(&data);
	
	ASSERT_EQ(0, string("-1234.5678").compare(itemValueWrapper.getItemValue().value));
	ASSERT_EQ(3, itemValueWrapper.getItemValue().quality);
	ASSERT_EQ(0, expectedTimestamp.compare(itemValueWrapper.getItemValue().timestamp));
	ASSERT_EQ(VT_R8, itemValueWrapper.getItemValue().dataType);

	char valueBuff[1024];
	char timestampBuff[1024];
	int qualityBuff;
	int typeBuff;

	itemValueWrapper.duplicateTo(1024, valueBuff, qualityBuff, typeBuff, timestampBuff);
	cout << "duplicated value is ["<<valueBuff<<"]" << endl;
	cout << "expected timestampis ["<<timestampBuff<<"] duplicated timestamp is ["<<timestampBuff<<"]" << endl;

	ASSERT_EQ(0, string("-1234.5678").compare(valueBuff));
	ASSERT_EQ(3, qualityBuff);
	ASSERT_EQ(0, expectedTimestamp.compare(timestampBuff));
	ASSERT_EQ(VT_R8, typeBuff);
}
