package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import static org.apache.commons.lang.StringUtils.isBlank

import org.opcfoundation.ua.builtintypes.DataValue

import static ch.cern.opc.common.Log.*
import org.opcfoundation.ua.builtintypes.NodeId;

class Node 
{
	private final NodeId uaId
	
	def Node(final def nodeId)
	{
		uaId = NodeId.decode(nodeId)
	}
	
	def getId()
	{
		return uaId.toString()
	}
	
	def getSyncValue()
	{
		DataValue[] values = UaClient.instance().readNodeValue(uaId)[0]
		logDebug("Received [${values.length}] values for node [${id}]")
		return new ValueWrapper(values[0])
	}
	
	def setSyncValue(def value)
	{
		return UaClient.instance().writeNodeValueSync(uaId, value)
	}
	
	def setAsyncValue(def value)
	{
		return UaClient.instance().writeNodeValueAsync(uaId, value)
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
