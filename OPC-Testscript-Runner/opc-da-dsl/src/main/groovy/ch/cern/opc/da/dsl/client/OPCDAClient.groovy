package ch.cern.opc.da.dsl.client

import ch.cern.opc.da.dsl.async.OPCDAUpdateHandler;
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.dsl.common.client.UpdateHandler;
import ch.cern.opc.client.OPCDAAsyncUpdateCallback
import ch.cern.opc.client.OPCDAClientInstance
import static ch.cern.opc.common.Log.*;


class OPCDAClient implements GenericClient 
{
	private OPCDAAsyncUpdateCallback daCallback = null
	
	@Override
	public String getLastError() 
	{
		return OPCDAClientInstance.instance.lastError
	}

	@Override
	public void registerForAsyncUpdates(UpdateHandler genericHandler) 
	{
		logInfo("OPCDA client - registering handler for asynchronous updates")
		daCallback = new OPCDAUpdateHandler(genericHandler)
		OPCDAClientInstance.instance.registerAsyncUpdate(daCallback)
	}

	@Override
	public void cleanUp() 
	{
		OPCDAClientInstance.instance.end();
	}
}
