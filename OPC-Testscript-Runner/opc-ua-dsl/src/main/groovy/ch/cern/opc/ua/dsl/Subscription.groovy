package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import static ch.cern.opc.common.Log.*

class Subscription 
{
	private def name
	private def isStarted
	
	def Subscription(name)
	{
		this.name = name
		
		isStarted = UaClient.instance().startSubscription(name)
		
		if(isStarted)
		{
			logInfo("OPCUA client started subscription [${name}]")
		} 
		else
		{
			logError("OPCUA client failed to start subscription [${name}]")
		}
	}
	
	def getIsStarted()
	{ 
		return isStarted
	}
}
