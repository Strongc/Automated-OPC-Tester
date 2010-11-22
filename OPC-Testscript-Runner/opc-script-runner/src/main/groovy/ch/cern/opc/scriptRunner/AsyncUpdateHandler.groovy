package ch.cern.opc.scriptRunner

import ch.cern.opc.client.AsyncUpdateCallback;
import ch.cern.opc.client.ClientInstance
import static ch.cern.opc.common.Log.*

class AsyncUpdateHandler implements AsyncUpdateCallback 
{
	private def registered = false
	
	def register()
	{
		if(!registered)
		{
			registered = true
			logInfo('registering async callback')
			ClientInstance.instance.registerAsyncUpdate(this)
		}
	}
	
	@Override
	public int onUpdate(String itemPath, String value) 
	{
		if(itemPath == null || value == null)
		{
			System.out.println("WARNING onUpdate called with null - item null [${itemPath==null?'Y':'N'}] value null [${value==null?'Y':'N'}]")
			return 0
		}
		else
		{
			logInfo("asyncUpdateHander.onUpdate called for item [${itemPath}] value [${value}]")
			return 1
		}
	}
}
