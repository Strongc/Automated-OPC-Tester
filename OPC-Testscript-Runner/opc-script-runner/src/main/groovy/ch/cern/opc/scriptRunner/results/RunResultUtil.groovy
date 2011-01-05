package ch.cern.opc.scriptRunner.results

import org.apache.commons.lang.BooleanUtils

protected class RunResultUtil 
{
	final static def NULL_MSG = "null assertion message"
	final static def EMPTY_MSG = "empty assertion message"
	
	private static final def ACCEPTED_FALSE_STRINGS = ['false', 'FALSE', 'False', '0', 'Zero', 'zero']
	private static final def ACCEPTED_TRUE_STRINGS = ['-1', '1']
	
	public static enum AnalyzedBooleanType {TRUE, FALSE, NEITHER}

	def static formatMessage(message)	
	{
		if(message == null)
		{
			return NULL_MSG
		}
		
		if(message.trim().empty)
		{
			return EMPTY_MSG
		}
		
		return message
	}
	
	static AnalyzedBooleanType toBoolean(actual)
	{
		if(actual != null)
		{
			if(String.class.equals(actual.class))
			{
				if(BooleanUtils.toBoolean(actual))
				{
					return AnalyzedBooleanType.TRUE
				}
				else if(isNumericalString(actual))
				{
					return (Integer.parseInt(actual) != 0? AnalyzedBooleanType.TRUE: AnalyzedBooleanType.FALSE)
				}
				else
				{
					if(ACCEPTED_FALSE_STRINGS.contains(actual))
					{
						return AnalyzedBooleanType.FALSE
					}
					else if(ACCEPTED_TRUE_STRINGS.contains(actual))
					{
						return AnalyzedBooleanType.TRUE
					}
					else
					{
						return AnalyzedBooleanType.NEITHER
					}
				}
			}
			else
			{
				if(actual.asBoolean())
				{
					return AnalyzedBooleanType.TRUE
				}
				else
				{
					return AnalyzedBooleanType.FALSE
				}
			}
		}
		
		return AnalyzedBooleanType.NEITHER
	}
	
	private static def isNumericalString(string)
	{
		try
		{
			Integer.parseInt(string)
			return true
		}
		catch(NumberFormatException e)
		{
			return false	
		}
	}

}
