package ch.cern.opc.ua.dsl.client

import org.apache.commons.lang.NotImplementedException

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.dsl.common.client.Client
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.common.Log.*

class OPCUAClient implements Client
{
	
	@Override
	public String getLastError() 
	{
		return UaClient.instance().lastError
	}

	@Override
	public void setUpdateHandler(UpdateHandler handler) 
	{
		logError("OPCUAClient.setUpdateHandler called with class [${handler.class.simpleName}] Async results handling not plumbed in yet")
	}
}
