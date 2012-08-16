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

using namespace std;
using namespace Utils;

TEST(UtilsTest, testVariantToStringConverterForString)
{
	OPCItemData data;
	data.vDataValue = _variant_t("I am a string");

	string result = VariantToStringConverter(data.vDataValue);

	ASSERT_EQ(0, string("I am a string").compare(result));
}

TEST(UtilsTest, testVariantToStringConverterForFloat)
{
	OPCItemData data;
	data.vDataValue = _variant_t(3.1415927);

	string result = VariantToStringConverter(data.vDataValue);

	ASSERT_EQ(0, string("3.1415927").compare(result));
}

TEST(UtilsTest, testVariantToStringConverterForFalseBool)
{
	OPCItemData data;
	data.vDataValue = _variant_t(false);

	string result = VariantToStringConverter(data.vDataValue);

	ASSERT_EQ(0, string("0").compare(result));
}

TEST(UtilsTest, testVariantToStringConverterForTrueBool)
{
	OPCItemData data;
	data.vDataValue = _variant_t(true);

	string result = VariantToStringConverter(data.vDataValue);

	// can only assert that string value is something other than 0
	ASSERT_NE(0, string("0").compare(result));
}

TEST(UtilsTest, testVariantToStringConverter)
{
	_variant_t wrappedVariant(123);
	std::string result = VariantToStringConverter(wrappedVariant.GetVARIANT());

	ASSERT_EQ(0, std::string("123").compare(result));
}

TEST(UtilsTest, testConvertIntToVarType)
{
	VARTYPE vt;
	ASSERT_FALSE(ConvertIntToVarType(-1, vt));
	ASSERT_EQ(VT_EMPTY, vt);
	
	ASSERT_TRUE(ConvertIntToVarType(11, vt));
	ASSERT_EQ(VT_BOOL, vt);

	ASSERT_TRUE(ConvertIntToVarType(2, vt));
	ASSERT_EQ(VT_I2, vt);

	ASSERT_TRUE(ConvertIntToVarType(3, vt));
	ASSERT_EQ(VT_I4, vt);

	ASSERT_TRUE(ConvertIntToVarType(4, vt));
	ASSERT_EQ(VT_R4, vt);

	ASSERT_TRUE(ConvertIntToVarType(8, vt));
	ASSERT_EQ(VT_LPSTR, vt);
}

TEST(UtilsTest, testConvertStringAndTypeToCorrectVariant)
{
	_variant_t i2Var("100");
	ASSERT_EQ(S_OK, VariantChangeType(&i2Var.GetVARIANT(), &i2Var.GetVARIANT(), VARIANT_ALPHABOOL, VT_I2));
	ASSERT_EQ(VT_I2, i2Var.vt);
	ASSERT_EQ(100, static_cast<int>(i2Var));

	_variant_t i4Var("1000");
	ASSERT_EQ(S_OK, VariantChangeType(&i4Var.GetVARIANT(), &i4Var.GetVARIANT(), VARIANT_ALPHABOOL, VT_I4));
	ASSERT_EQ(VT_I4, i4Var.vt);
	ASSERT_EQ(1000, static_cast<int>(i4Var));

	_variant_t fltVar("1.23");
	ASSERT_EQ(S_OK, VariantChangeType(&fltVar.GetVARIANT(), &fltVar.GetVARIANT(), VARIANT_ALPHABOOL, VT_R4));
	ASSERT_EQ(VT_R4, fltVar.vt);
	ASSERT_TRUE(fabs(1.23 - static_cast<float>(fltVar)) < 0.0001);

	_variant_t boolVarTrue("true");
	ASSERT_EQ(S_OK, VariantChangeType(&boolVarTrue.GetVARIANT(), &boolVarTrue.GetVARIANT(), VARIANT_ALPHABOOL, VT_BOOL));
	ASSERT_EQ(VT_BOOL, boolVarTrue.vt);
	ASSERT_TRUE(static_cast<bool>(boolVarTrue));

	_variant_t boolVarFalse("false");
	ASSERT_EQ(S_OK, VariantChangeType(&boolVarFalse.GetVARIANT(), &boolVarFalse.GetVARIANT(), VARIANT_ALPHABOOL, VT_BOOL));
	ASSERT_EQ(VT_BOOL, boolVarFalse.vt);
	ASSERT_FALSE(static_cast<bool>(boolVarFalse));
}

TEST(VariantTypeConversionTest, testConvertStringToString)
{
	const char* pBuff = "I am a string";
	_variant_t wrappedVariant(pBuff);

	(_bstr_t)wrappedVariant;

	VARIANT v = (VARIANT)wrappedVariant;
	ASSERT_EQ(VT_BSTR, v.vt);
}

TEST(RecordErrorTest, testSimpleErrorIsRecorded)
{
	RecordError("I am the error");
	cout << gstrLastError << endl;
	ASSERT_EQ(0, strcmp("I am the error", gstrLastError.c_str()));
}

TEST(RecordErrorTest, testFormattedErrorIsRecorded)
{
	RecordError("Error [%d] [%s]", 69, "woohoo");
	cout << gstrLastError << endl;
	ASSERT_EQ(0, strcmp("Error [69] [woohoo]", gstrLastError.c_str()));
}