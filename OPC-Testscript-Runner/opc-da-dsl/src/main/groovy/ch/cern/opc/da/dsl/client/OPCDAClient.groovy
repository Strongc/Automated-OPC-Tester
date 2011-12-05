package ch.cern.opc.da.dsl.client

import ch.cern.opc.da.dsl.async.AsyncUpdateHandler;
import ch.cern.opc.dsl.common.client.Client
import ch.cern.opc.dsl.common.client.UpdateHandler;
import ch.cern.opc.client.OPCDAClientInstance
import static ch.cern.opc.common.Log.*;


class OPCDAClient implements Client 
{
	private AsyncUpdateHandler asyncHandler = null;
	
	@Override
	public String getLastError() 
	{
		return OPCDAClientInstance.instance.lastError
	}

	@Override
	public void setUpdateHandler(UpdateHandler genericHandler) 
	{
		logInfo("OPCDA client - registering handler for asynchronous updates")
		asyncHandler = new AsyncUpdateHandler(genericHandler)
	}

	@Override
	public void cleanUp() 
	{
		OPCDAClientInstance.instance.end();
	}
}
