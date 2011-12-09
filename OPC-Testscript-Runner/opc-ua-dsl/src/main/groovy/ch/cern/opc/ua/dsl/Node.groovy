package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import static org.apache.commons.lang.StringUtils.isBlank
import org.opcfoundation.ua.builtintypes.DataValue
import static ch.cern.opc.common.Log.*

class Node 
{
	private def id
	
	def Node(final def nodeId)
	{
		if(isBlank(nodeId))
		{
			throw new IllegalArgumentException("invalid node id [${id}]")
		}
		
		this.id = nodeId
	}
	
	def getSyncValue()
	{
		DataValue[] values = UaClient.instance().readNodeValue(id)[0]
		logDebug("Received [${values.length}] values for node [${id}]")
		return new ValueWrapper(values[0])
	}
	
	def setSyncValue(def value)
	{
		return UaClient.instance().writeNodeValue(id, value)
	}
	
	def assertAsyncEquals(message, timeout, expectedValue)
	{
		ScriptContext.instance.assertAsyncEquals(message, timeout, expectedValue, id)
	}
	
	def assertAsyncNotEquals(message, timeout, antiExpectedValue)
	{
		ScriptContext.instance.assertAsyncNotEquals(message, timeout, antiExpectedValue, id)
	}
}
