package ch.cern.opc.da.dsl

import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.common.Log

class Group 
{
	private def name
	def items = [:]
	
	def private final static ANY_DEPTH = '[a-zA-Z0-9\\.\\_\\-]*'
	def private final static WITHIN_DOTS = '[a-zA-Z0-9\\_\\-]*'
	def private final static STAR_REG_EXPS = ['**':ANY_DEPTH, '*':WITHIN_DOTS]
	
	def public final static int GROUP_REFRESH_RATE_MS = 1000
	
	def Group(name, refreshRateMs = GROUP_REFRESH_RATE_MS)
	{
		if(name == null) throw new IllegalArgumentException("Group names cannot be null")
		if(name.toString().empty) throw new IllegalArgumentException("Group names cannot be empty")
		
		this.name = name.toString()
		OPCDAClientInstance.instance.createGroup(name, refreshRateMs)
	}
	
	private def addItem(path)
	{
		OPCDAClientInstance.instance.addItem(name, path)
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

		OPCDAClientInstance.instance.getItemNames().each 
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
		OPCDAClientInstance.instance.destroyGroup(name)
		ScriptContext.instance.destroyGroup(name)
	}

	private def replaceStarsWithRegexps(def text)
	{
		def chars = text.toString().chars
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
