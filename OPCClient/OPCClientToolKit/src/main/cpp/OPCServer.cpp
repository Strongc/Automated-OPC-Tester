/*
OPCClientToolKit
Copyright (C) 2005 Mark C. Beharrell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*/

#include "OPCServer.h"
#include "opcda.h"
#include <iostream>
#include <sstream>

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;
using namespace std;


WCHAR emptyString[] = {0};








COPCServer::COPCServer(ATL::CComPtr<IOPCServer> &opcServerInterface){
	iOpcServer = opcServerInterface;

	HRESULT res = opcServerInterface->QueryInterface(IID_IOPCBrowseServerAddressSpace, (void**)&iOpcNamespace);
	if (FAILED(res)){
		throw OPCException("Failed to obtain IID_IOPCBrowseServerAddressSpace interface",res);
	}

	res = opcServerInterface->QueryInterface(IID_IOPCItemProperties, (void**)&iOpcProperties);
	if (FAILED(res)){
		throw OPCException("Failed to obtain IID_IOPCItemProperties interface",res);
	}
}



COPCServer::~COPCServer()
{
}



COPCGroup *COPCServer::makeGroup(const CString & groupName, bool active, unsigned long reqUpdateRate_ms, unsigned long &revisedUpdateRate_ms, float deadBand){
	return new COPCGroup(groupName, active, reqUpdateRate_ms, revisedUpdateRate_ms, deadBand, *this);
}

void COPCServer::browseFlatNamespace(CAtlArray<CAtlString>* tmpOpcItemNames)
{
	  ATL::CComPtr<IEnumString> iEnum; 

      HRESULT result = iOpcNamespace->BrowseOPCItemIDs(OPC_FLAT, emptyString, VT_EMPTY, 0, (&iEnum)); 
      if (FAILED(result)){ 
         return; 
      } 

	  WCHAR* str; 
	  ULONG strSize; 

      while((result = iEnum->Next(1, &str, &strSize)) == S_OK) 
      { 
         WCHAR* fullName; 
         result = iOpcNamespace->GetItemID(str, &fullName); 
         if (SUCCEEDED(result)){ 
            USES_CONVERSION; 
            char* cStr = OLE2T(fullName); 
            tmpOpcItemNames->Add(cStr); 
            COPCClient::comFree(fullName); 
         } 
         COPCClient::comFree(str); 
      } 
}

std::string COPCServer::narrow(const WCHAR* utf8String) const
{
	std::wstring str(utf8String);
	std::ostringstream stm ;    
	const ctype<char>& ctfacet = use_facet< ctype<char> >( stm.getloc() ) ;    
	for( size_t i=0 ; i<str.size() ; ++i )
	{
		stm << ctfacet.narrow( str[i], 0 );
	}
	return stm.str();
}

void COPCServer::browseLeaves(WCHAR* branchName, CAtlArray<CAtlString>* opcItemNames)
{
	USES_CONVERSION; 	

	// Browse LEAF 
	ATL::CComPtr<IEnumString> iEnumLeaf; 
	HRESULT result = iOpcNamespace->BrowseOPCItemIDs(OPC_LEAF, emptyString, VT_EMPTY, 0, &iEnumLeaf); 
	if (FAILED(result))
	{ 
		log_ERROR("Failed to browse leaf nodes for OPC branch [", narrow(branchName),"]");
		return; 		
	}

	log_NOTICE("processing leaf nodes for OPC branch [", narrow(branchName),"]");

	WCHAR* str; 
	ULONG strSize; 

	while((result = iEnumLeaf->Next(1, &str, &strSize)) == S_OK) 
	{ 
		WCHAR* fullName; 
		if (SUCCEEDED(iOpcNamespace->GetItemID(str, &fullName))) 
		{ 			
			char* cStr = OLE2T(fullName); 
			opcItemNames->Add(cStr); 
			COPCClient::comFree(fullName); 
		} 
		COPCClient::comFree(str); 
	} 

	log_NOTICE("leaf nodes processed for OPC branch [", narrow(branchName),"]");
}

void COPCServer::browseBranch(WCHAR* branchName, CAtlArray<CAtlString>* opcItemNames)
{
	USES_CONVERSION; 	

	log_NOTICE("browsing branch [", narrow(branchName),"], changing browse position to [", narrow(branchName),"] direction [DOWN]");
	iOpcNamespace->ChangeBrowsePosition(OPC_BROWSE_DOWN, branchName); 
    
	browseLeaves(branchName, opcItemNames);

	// then browse sub branches...
	ATL::CComPtr<IEnumString> iEnumBranch; 
	HRESULT result = iOpcNamespace->BrowseOPCItemIDs(OPC_BRANCH, emptyString, VT_EMPTY, 0, (&iEnumBranch)); 
	if (FAILED(result))   
	{ 
		log_ERROR("Failed to browse sub branches nodes for OPC branch [", narrow(branchName),"]");
		return; 
	} 
	log_NOTICE("processing sub branch nodes for OPC branch [", narrow(branchName),"]");

	WCHAR* str; 
	ULONG strSize; 
	while((result = iEnumBranch->Next(1, &str, &strSize)) == S_OK) 
	{ 		
		browseBranch(str, opcItemNames);		
	} 

	log_NOTICE("branch browsed, browsing up - direction [UP]");
	iOpcNamespace->ChangeBrowsePosition(OPC_BROWSE_UP, emptyString);
}


void COPCServer::browseNamespace(CAtlArray<CAtlString>* opcItemNames) 
{ 
   OPCNAMESPACETYPE nameSpaceType;      
   HRESULT result = iOpcNamespace->QueryOrganization(&nameSpaceType); 

   if (nameSpaceType == OPC_NS_FLAT) 
   {
	   log_NOTICE("Browsing flat namespace");
	   browseFlatNamespace(opcItemNames);
   } 
   else if (nameSpaceType == OPC_NS_HIERARCHIAL) 
   { 
	   log_NOTICE("Browsing hierarchical namespace");
	   browseBranch(emptyString/*root*/, opcItemNames);
   } 
} 

void COPCServer::getItemNames(CAtlArray<CString> & opcItemNames){ 
   if (!iOpcNamespace) 
   {
	   log_ERROR("Catasrophic failure - unable to get handle to namespace browser interface - IOPCBrowseServerAddressSpace");
	   throw OPCException("Catasrophic failure - unable to get handle to namespace browser interface - IOPCBrowseServerAddressSpace");
   }
    
   browseNamespace(&opcItemNames); 
} 

void COPCServer::getStatus(ServerStatus &status){
	OPCSERVERSTATUS *serverStatus;
	HRESULT result = iOpcServer->GetStatus(&serverStatus);
	if (FAILED(result)){
		throw OPCException("Failed to get status");
	}

	status.ftStartTime = serverStatus->ftStartTime;
    status.ftCurrentTime = serverStatus->ftCurrentTime;
    status.ftLastUpdateTime = serverStatus->ftLastUpdateTime;
    status.dwServerState = serverStatus->dwServerState;
    status.dwGroupCount = serverStatus->dwGroupCount;
    status.dwBandWidth = serverStatus->dwBandWidth;
    status.wMajorVersion = serverStatus->wMajorVersion;
    status.wMinorVersion = serverStatus->wMinorVersion;
    status.wBuildNumber = serverStatus->wBuildNumber;
	if (serverStatus->szVendorInfo != NULL){
		USES_CONVERSION;
		status.vendorInfo = OLE2T(serverStatus->szVendorInfo);
		COPCClient::comFree(serverStatus->szVendorInfo);
	}
	COPCClient::comFree(serverStatus);
}