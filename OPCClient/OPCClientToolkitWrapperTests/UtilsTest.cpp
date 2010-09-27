#include "common.h"
#include <iostream>
#include <time.h>
#include "comutil.h"

#include "OPCClient.h"
#include "OPCHost.h"
#include "OPCServer.h"
#include "OPCGroup.h"

#include "Utils.h"
#include "gtest\gtest.h"


TEST(UtilsTest, testConvertOPCItemDataValueToCharArrayForString)
{
	OPCItemData data;
	data.vDataValue = _variant_t("I am a string");

	char buff[100];
	ConvertOPCItemDataValueToCharArray(data, buff, 100);

	ASSERT_EQ(0, strcmp("I am a string", buff));
}

TEST(UtilsTest, testConvertOPCItemDataValueToCharArrayForFloat)
{
	OPCItemData data;
	data.vDataValue = _variant_t(3.1415927);

	char buff[100];
	ConvertOPCItemDataValueToCharArray(data, buff, 100);

	ASSERT_EQ(0, strcmp("3.1415927", buff));
}

TEST(UtilsTest, testConvertOPCItemDataValueToCharArrayForFalseBool)
{
	OPCItemData data;
	data.vDataValue = _variant_t(false);

	char buff[100];
	ConvertOPCItemDataValueToCharArray(data, buff, 100);

	ASSERT_EQ(0, strcmp("0", buff));
}

TEST(UtilsTest, testConvertOPCItemDataValueToCharArrayForTrueBool)
{
	OPCItemData data;
	data.vDataValue = _variant_t(true);

	char buff[100];
	ConvertOPCItemDataValueToCharArray(data, buff, 100);

	// can only assert that string value is something other than 0
	ASSERT_NE(0, strcmp("0", buff));
}

TEST(VariantTypeConversionTest, testConvertStringToString)
{
	const char* pBuff = "I am a string";
	_variant_t wrappedVariant(pBuff);

	(_bstr_t)wrappedVariant;

	VARIANT v = (VARIANT)wrappedVariant;
	ASSERT_EQ(VT_BSTR, v.vt);
}
