package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance
import ch.cern.opc.common.Log

@Mixin([Assertions, Log])
class ScriptContext 
{
	def groups = [:]
	
	private def createGroup(name)
	{
		groups[name] = new Group(name)
		return groups[name]
	}
	
	private def findGroup(name)
	{
		return groups[name]
	}
	
	def group(name)
	{
		if(findGroup(name) == null)
		{
			createGroup(name)
		}
		return findGroup(name)
	}
	
	def init(host, server)
	{
		ClientInstance.instance.init(host, server)
	}
	
	def getLastError()
	{
		return ClientInstance.instance.lastError
	}
}
