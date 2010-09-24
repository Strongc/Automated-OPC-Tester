package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance

class Group 
{
	private def name
	def items = [:]
	
	def Group(name)
	{
		if(name == null) throw new IllegalArgumentException("Group names cannot be null")
		if(name.empty) throw new IllegalArgumentException("Group names cannot be empty")
		
		this.name = name
		ClientInstance.instance.createGroup(name, 1000)
	}
	
	private def addItem(path)
	{
		ClientInstance.instance.addItem(name, path)
		items[path] = new Item(name, path)
	}
	
	private def findItem(path)
	{
		return items[path]
	}
	
	def item(path)
	{
		if(items[path] == null)
		{
			addItem(path)
		}
		return items[path]
	}
}
