package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance
import ch.cern.opc.common.Log
import ch.cern.opc.scriptRunner.results.RunResults

@Mixin(Log)
class Item 
{
	def groupName
	def path
	
	def Item(groupName, path)
	{
		if(groupName == null) throw new IllegalArgumentException("groupName must not be null")
		if(groupName.empty) throw new IllegalArgumentException("groupName must not be null")
		
		if(path == null) throw new IllegalArgumentException("path must not be null")
		if(path.empty) throw new IllegalArgumentException("path must not be empty")
		
		this.path = path
		this.groupName = groupName
	}
	
	def getSyncValue()
	{
		return ClientInstance.instance.readItemSync(groupName, path)
	}
	
	def setSyncValue(value)
	{
		return ClientInstance.instance.writeItemSync(groupName, path, value)
	}
	
	def setAsyncValue(value)
	{
		return ClientInstance.instance.writeItemAsync(groupName, path, value)
	}
	
	def assertEquals(message, expectedValue)
	{
		ScriptContext.instance.assertEquals(message, expectedValue, syncValue)
	}
	
	def assertAsyncEquals(message, timeout, expectedValue)
	{
		ScriptContext.instance.assertAsyncEquals(message, timeout, expectedValue, path)
	}
}
