package ch.cern.opc.ua.dsl

import org.opcfoundation.ua.builtintypes.DataValue

/**
 * Class wraps a DataValue or DataValue[] to lower the complexity
 * of DataValue objects for the DSL
 * 
 * @author bfarnham
 *
 */
class ValueWrapper 
{
	def final wrappedValue
	
	def ValueWrapper(DataValue value)
	{
		this.wrappedValue = value	
	}
	
	def getValue()
	{
		if(wrappedValue == null) return 'null'
		
		def variant = wrappedValue.getValue()
		def object = variant.getValue() 
		return object.toString()
	}
	
	def getServerTimestamp()
	{
		if(wrappedValue == null) return 'null'
		return wrappedValue.getServerTimestamp().toString()
	}
	
	def getSourceTimestamp()
	{
		if(wrappedValue == null) return 'null'
		return wrappedValue.getSourceTimestamp().toString()
	}
	
	def getType()
	{
		if(wrappedValue == null) return 'null'
		def variant = wrappedValue.getValue()
		return variant.compositeClass.simpleName
	}
	
	@Override
	public String toString() 
	{
		return getValue()
	}
}
