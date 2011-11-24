package ch.cern.opc.da.dsl.async

import ch.cern.opc.client.OPCDAAsyncUpdateCallback;
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.common.Log.*

class AsyncUpdateHandler implements OPCDAAsyncUpdateCallback 
{
	final UpdateHandler genericHandler
	
	def AsyncUpdateHandler(UpdateHandler genericHandler)
	{
		this.genericHandler = genericHandler
		register()
	}
	
	private def register()
	{
		logInfo('registering async callback with client dll')
		OPCDAClientInstance.instance.registerAsyncUpdate(this)
	}
	
	@Override
	public int onUpdate(String itemPath, String value) 
	{
		if(itemPath == null || value == null)
		{
			logWarning("WARNING onUpdate called with null - item null [${itemPath==null?'Y':'N'}] value null [${value==null?'Y':'N'}]")
			return 0
		}
		else
		{
			logTrace("asyncUpdateHandler.onUpdate called for item [${itemPath}] value [${value}] thread [${Thread.currentThread().id}]")
			genericHandler.onUpdate(itemPath, null, value)
			return 1
		}
	}
}
