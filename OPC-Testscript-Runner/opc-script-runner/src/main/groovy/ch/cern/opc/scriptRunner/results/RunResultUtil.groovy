package ch.cern.opc.scriptRunner.results

import org.apache.commons.lang.BooleanUtils

protected class RunResultUtil 
{
	final static def NULL_MSG = "null assertion message"
	final static def EMPTY_MSG = "empty assertion message"
	
	private static final def ACCEPTED_FALSE_STRINGS = ['false', 'FALSE', 'False', '0', 'Zero', 'zero']
	
	public enum AnalyzedBooleanType {TRUE, FALSE, NEITHER}

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
				else
				{
					if(ACCEPTED_FALSE_STRINGS.contains(actual))
					{
						return AnalyzedBooleanType.FALSE
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

}
