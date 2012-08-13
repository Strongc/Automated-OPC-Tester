package ch.cern.opc.da.dsl.async

import ch.cern.opc.client.OPCDAAsyncUpdateCallback;
import ch.cern.opc.client.OPCDAClientApi;
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.client.Update
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.common.Log.*

class OPCDAUpdateHandler implements OPCDAAsyncUpdateCallback 
{
	final UpdateHandler genericHandler
	
	def OPCDAUpdateHandler(UpdateHandler genericHandler)
	{
		this.genericHandler = genericHandler
	}
	
	@Override
	public int onUpdate(Update update) 
	{
		if(update.itemPath == null || update.value == null)
		{
			logWarning("WARNING onUpdate called with null - item null [${update.itemPath==null?'Y':'N'}] value null [${update.value==null?'Y':'N'}]")
			return 0
		}
		else
		{
			logTrace("onUpdate called for item [${update.itemPath}] value [${update.value}] thread [${Thread.currentThread().id}]")
			genericHandler.onUpdate(update.itemPath, null, update.value, update.quality, update.type, update.timestamp)
			return 1
		}
	}
}
