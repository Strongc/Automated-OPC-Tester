package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import static ch.cern.opc.common.Log.*
import org.opcfoundation.ua.builtintypes.NodeId;

/**
 * Class wraps a real UA client subscription for the DSL. Note it doesn't
 * actually hold a reference to the real subscription - interactions with
 * the real subscription are handled via the UAClient interface.
 * 
 * @author bfarnham
 *
 */
class Subscription 
{
	private def name
	private def isStarted
	private def monitoredNodes = [:]
	
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
	
	def delete()
	{
		if(UaClient.instance().deleteSubscription(name))
		{
			logInfo("OPCUA client deleted subscription [${name}]")
		}
		else
		{
			logError("OPCUA client failed to delete subscription [${name}]")
		}
		isStarted = false;
	}
	
	def monitor(Node... nodes)
	{
		def nodeIds = []
		nodes.each{nodeIds << it.uaId}
		
		def uniqueNodeIds = nodeIds as Set
		
		UaClient.instance().monitorNodeValues(name, uniqueNodeIds as NodeId[])
	}
	
}
