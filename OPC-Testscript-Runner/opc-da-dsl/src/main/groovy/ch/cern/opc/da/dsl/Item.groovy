package ch.cern.opc.da.dsl

import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.common.Log

class Item 
{
	def groupName
	def path
	
	def Item(groupName, path)
	{
		if(groupName == null) throw new IllegalArgumentException("groupName must not be null")
		if(groupName.empty) throw new IllegalArgumentException("groupName must not be null")
		
		if(path == null) throw new IllegalArgumentException("path must not be null")
		if(path.toString().empty) throw new IllegalArgumentException("path must not be empty")
		
		this.path = path.toString()
		this.groupName = groupName
	}
	
	def getSyncValue()
	{
		return OPCDAClientInstance.instance.readItemSync(groupName, path).value
	}
	
	def getDatatype()
	{
		return OPCDAClientInstance.instance.getItemDatatype(groupName, path)
	}
	
	def getAccessRights()
	{
		return OPCDAClientInstance.instance.getItemAccessRights(groupName, path)
	}
	
	def setSyncValue(value)
	{
		return OPCDAClientInstance.instance.writeItemSync(groupName, path, value)
	}
	
	def getQuality()
	{
		return OPCDAClientInstance.instance.readItemSync(groupName, path).quality
	}
		
	def setAsyncValue(value)
	{
		return OPCDAClientInstance.instance.writeItemAsync(groupName, path, value)
	}
	
	def assertEquals(message, expectedValue)
	{
		ScriptContext.instance.assertEquals(message, expectedValue, syncValue)
	}
	
	def assertTrue(message)
	{
		ScriptContext.instance.assertTrue(message, syncValue)
	}
	
	def assertFalse(message)
	{
		ScriptContext.instance.assertFalse(message, syncValue)
	}
	
	def assertAsyncEquals(message, timeout, expectedValue)
	{
		ScriptContext.instance.assertAsyncEquals(message, timeout, expectedValue, path)
	}
	
	def assertAsyncNotEquals(message, timeout, antiExpectedValue)
	{
		ScriptContext.instance.assertAsyncNotEquals(message, timeout, antiExpectedValue, path)
	}
	
	def assertQuality(message, expectedQuality)
	{
		ScriptContext.instance.assertQuality(message, expectedQuality, quality)		
	}
	
	def assertDatatype(message, expectedDatatype)
	{
		ScriptContext.instance.assertDatatype(message, expectedDatatype, datatype)
	}
	
	def assertAccessRights(message, expectedAccessRights)
	{
		ScriptContext.instance.assertAccessRights(message, expectedAccessRights, accessRights)
	}
	
	def assertAsyncQuality(message, timeout, expectedQuality)
	{
		ScriptContext.instance.assertAsyncQuality(message, timeout, expectedQuality, path)
	}
}
