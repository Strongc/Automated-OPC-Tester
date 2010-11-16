#pragma once

#include <cstdarg>
#include <cstdio>
#include "OPCItemData.h"
#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;

extern CString gstrLastError;

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
}

static bool ConvertIntToVarType(const int nVarType, VARTYPE& vt)
{
	switch(nVarType)
	{
	case 11:
		vt = VT_BOOL;
		return true;
	case 3:
		vt = VT_I4;
		return true;
	case 2:
		vt = VT_I2;
		return true;
	case 4:
		vt = VT_R4;
		return true;
	case 8:
		vt = VT_LPSTR;
		return true;
	default:
		vt = VT_EMPTY;
		log_ERROR("Failed to convert canonical OPC datatype [", pantheios::integer(nVarType),"]to variant datatype");
		return false;
	}
}

static void RecordError(char* format, ...)
{
	log_ERROR("RecordError called - format [", format,"]");
	va_list args;
	va_start(args, format);

	char buff[1028];
    vsprintf_s(buff, 1028, format, args);

	gstrLastError = buff;
	log_ERROR(buff);

    va_end(args);
}