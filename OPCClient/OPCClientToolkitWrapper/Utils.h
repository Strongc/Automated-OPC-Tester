#pragma once

#include "OPCItemData.h"


static char* ConvertVariantToCharArray(VARIANT& variant, char* pBuff, size_t szBuff)
{
	_variant_t vrtValue(variant);

	_bstr_t bstValue = static_cast<_bstr_t>(vrtValue);

	char* pValue = static_cast<char*>(bstValue);

	strcpy_s(pBuff, szBuff, pValue);

	return pBuff;
}

static char* ConvertOPCItemDataValueToCharArray(OPCItemData& data, char* pBuff, size_t szBuff)
{
	return ConvertVariantToCharArray(data.vDataValue, pBuff, szBuff);
	/*
	_variant_t vrtValue(data.vDataValue);

	_bstr_t bstValue = (_bstr_t)vrtValue;

	char* pValue = (char*)bstValue;

	strcpy_s(pBuff, szBuff, pValue);
	*/
}
