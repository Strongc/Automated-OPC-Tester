package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import static org.apache.commons.lang.StringUtils.isBlank
import org.opcfoundation.ua.builtintypes.DataValue
import static ch.cern.opc.common.Log.*

class Node 
{
	private def nodeId
	
	def Node(final def nodeId)
	{
		if(isBlank(nodeId))
		{
			throw new IllegalArgumentException("invalid node id [${nodeId}]")
		}
		
		this.nodeId = nodeId
	}
	
	def getSyncValue()
	{
		DataValue[] values = UaClient.instance().readNodeValue(nodeId)[0]
		logDebug("Received [${values.length}] values for node [${nodeId}]")
		return new ValueWrapper(values[0])
	}
	
	def setSyncValue(def value)
	{
		return UaClient.instance().writeNodeValue(nodeId, value)
	}
}
