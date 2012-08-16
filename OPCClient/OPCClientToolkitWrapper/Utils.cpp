#pragma once

#include "Utils.h"

#include "common.h"
#include <iostream>
#include "comutil.h"
#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>



using namespace pantheios;

extern std::string gstrLastError;

std::string Utils::VariantToStringConverter(const VARIANT& variant)
{
	_variant_t vrtValue(variant);

	_bstr_t bstValue = static_cast<_bstr_t>(vrtValue);

	return std::string(static_cast<char*>(bstValue));
}

bool Utils::ConvertIntToVarType(const int nVarType, VARTYPE& vt)
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

void Utils::RecordError(char* format, ...)
{
	va_list args;
	va_start(args, format);

	char buff[1028];
    vsprintf_s(buff, 1028, format, args);

	gstrLastError = std::string(buff);
	log_ERROR(buff);

    va_end(args);
}