package ch.cern.opc.da.dsl

import ch.cern.opc.client.OPCDAAsyncUpdateCallback
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.common.Log.*

class AsyncUpdateHandler implements OPCDAAsyncUpdateCallback 
{
	private def registered = false
	
	final UpdateHandler assertAsyncManager
	
	def AsyncUpdateHandler(UpdateHandler assertAsyncManager)
	{
		this.assertAsyncManager = assertAsyncManager
	}
	
	def register()
	{
		if(!registered)
		{
			registered = true
			logInfo('registering async callback')
			OPCDAClientInstance.instance.registerAsyncUpdate(this)
		}
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
			assertAsyncManager.asyncUpdate(itemPath, value)
			return 1
		}
	}
}
