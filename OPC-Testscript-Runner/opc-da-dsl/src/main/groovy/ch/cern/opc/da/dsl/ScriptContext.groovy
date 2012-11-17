package ch.cern.opc.da.dsl

import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.common.Log
import ch.cern.opc.dsl.common.results.RunResults;
import ch.cern.opc.da.dsl.client.OPCDAClient;

@Mixin([RunResults, Log, ScriptConstants])
class ScriptContext 
{
	private static def instance
	private def randomGenerator

	def static getInstance()
	{
		if(null == instance)
		{
			instance = new ScriptContext()
		}
		return instance
	}
	
	def groups = [:]
	
	def ScriptContext()
	{
		instance = this
		setClient(new OPCDAClient())
	}
	
	private def createGroup(name, refreshRateMs = Group.GROUP_REFRESH_RATE_MS)
	{
		groups[name] = new Group(name, refreshRateMs)
		return groups[name]
	}
	
	private def findGroup(name)
	{
		return groups[name]
	}
	
	def group(name, refreshRateMs = Group.GROUP_REFRESH_RATE_MS)
	{
		if(findGroup(name) == null)
		{
			createGroup(name, refreshRateMs)
		}
		return findGroup(name)
	}
	
	def destroyGroup(name)
	{
		groups.remove(name)
	}
	
	def init(host, server)
	{
		OPCDAClientInstance.instance.init(host, server)
	}
	
	def getLastError()
	{
		return OPCDAClientInstance.instance.lastError
	}
	
	private def getRandomGenerator()
	{
		if(randomGenerator == null)
		{
			randomGenerator = new Random()
		}
		
		return randomGenerator
	}
	
	def randomInt(limitA, limitB)
	{
		def diff = (limitA - limitB).abs()
		
		if(diff < 1) return limitA
		
		return [limitA, limitB].min() + getRandomGenerator().nextInt(diff);
	}
}
