package ch.cern.opc.ua.dsl.client

import org.apache.commons.lang.NotImplementedException

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.dsl.async.OPCUAUpdateHandler
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.common.Log.*

class OPCUAClient implements GenericClient
{
	
	@Override
	public String getLastError() 
	{
		return UaClient.instance().lastError
	}

	@Override
	public void registerForAsyncUpdates(UpdateHandler genericHandler) 
	{
		logInfo("OPCUA client - registering handler for asynchronous updates")
		def uaHandler = new OPCUAUpdateHandler(genericHandler)
		UaClient.instance().registerAsyncUpdate(uaHandler)
	}

	@Override
	public void cleanUp() 
	{
		logInfo("OPCUA client clean up - stopping session")
		UaClient.instance().stopSession()
		
	}
}
