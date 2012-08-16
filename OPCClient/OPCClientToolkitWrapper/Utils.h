#pragma once

#include <cstdarg>
#include <cstdio>

#include <atlbase.h>
#include <atlstr.h>
#include <atlcoll.h>
#include <string>

#include "OPCItemData.h"

#include "comutil.h"

extern std::string gstrLastError;

namespace Utils
{
	std::string VariantToStringConverter(const VARIANT& variant);
	bool ConvertIntToVarType(const int nVarType, VARTYPE& vt);
	void RecordError(char* format, ...);
}