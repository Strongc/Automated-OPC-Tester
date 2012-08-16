package ch.cern.opc.da.dsl.client

import ch.cern.opc.da.dsl.async.OPCDAUpdateHandler;
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.dsl.common.client.UpdateHandler;
import ch.cern.opc.client.OPCDAAsyncUpdateCallback
import ch.cern.opc.client.OPCDAClientInstance
import static ch.cern.opc.common.Log.*;


class OPCDAClient implements GenericClient 
{
	private OPCDAUpdateHandler updateHandler = null
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
		
		// set up handling updates out to wider Java
		updateHandler = new OPCDAUpdateHandler(genericHandler)
		updateHandler.startUpdaterThread();
		
		// setup handling 'landing zone' for updates direct from C++
		daCallback = new OPCDAAsyncUpdateCallback(updateHandler.updatesQueue)
		OPCDAClientInstance.instance.registerAsyncUpdate(daCallback)
	}

	@Override
	public void cleanUp() 
	{
		updateHandler.stopUpdaterThread();
		
		OPCDAClientInstance.instance.end();
	}
}
