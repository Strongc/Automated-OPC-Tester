package ch.cern.opc.scriptRunner

import ch.cern.opc.client.ClientInstance
import ch.cern.opc.common.Log

@Mixin(Log)
class Group 
{
	private def name
	def items = [:]
	
	def private final static ANY_DEPTH = '[a-zA-Z0-9\\.\\_\\-]*'
	def private final static WITHIN_DOTS = '[a-zA-Z0-9\\_\\-]*'
	def private final static STAR_REG_EXPS = ['**':ANY_DEPTH, '*':WITHIN_DOTS]
	
	def Group(name)
	{
		if(name == null) throw new IllegalArgumentException("Group names cannot be null")
		if(name.toString().empty) throw new IllegalArgumentException("Group names cannot be empty")
		
		this.name = name.toString()
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
	
	def items(patternText)
	{
		def matchingItems = []
        def pattern = ~/${replaceStarsWithRegexps(patternText)}/

		ClientInstance.instance.getItemNames().each 
		{itemPath->
			if(pattern.matcher(itemPath).matches())
			{
				matchingItems << item(itemPath)
			}
		}
		
		return matchingItems
	}
	
	def destroy()
	{
		ClientInstance.instance.destroyGroup(name)
		ScriptContext.instance.destroyGroup(name)
	}

	private def replaceStarsWithRegexps(def text)
	{
		def chars = text.chars
		def result = ""
		
		for(def i = chars.length-1; i >= 0; i--)
		{
			if(chars[i] != '*')
			{
				result = new String(chars[i]) + result
			}
			else
			{
				// expression '**' - note decrement extra place due to replacing 2 * chars
				if(chars[i] == '*' && i > 0 && chars[i-1] == '*')
				{
					result = STAR_REG_EXPS['**'] + result
					i--
				}
				// expression '*'
				else
				{
					result = STAR_REG_EXPS['*'] + result
				}
			}
		}
		
		return result
	}
}
