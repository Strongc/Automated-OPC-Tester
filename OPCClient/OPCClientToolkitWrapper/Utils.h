#pragma once

#include "OPCItemData.h"

static void ConvertOPCItemDataValueToCharArray(OPCItemData& data, char* pBuff, size_t szBuff)
{
	_variant_t vrtValue(data.vDataValue);

	_bstr_t bstValue = (_bstr_t)vrtValue;

	char* pValue = (char*)bstValue;

	strcpy_s(pBuff, szBuff, pValue);
}