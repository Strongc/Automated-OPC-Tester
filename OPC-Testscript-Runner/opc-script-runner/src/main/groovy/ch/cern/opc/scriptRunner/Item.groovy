package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance

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
}
