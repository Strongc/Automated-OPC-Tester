package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance
import ch.cern.opc.common.Log
import ch.cern.opc.scriptRunner.results.RunResults;

@Mixin([RunResults, Log])
class ScriptContext 
{
	private static def instance
	
	def static getInstance()
	{
		return instance
	}
	
	def groups = [:]
	
	def ScriptContext()
	{
		instance = this
	}
	
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
