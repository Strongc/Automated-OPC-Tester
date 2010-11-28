package ch.cern.opc.scriptRunner.results

class RunResultUtil 
{
	final static def NULL_MSG = "null assertion message";
	final static def EMPTY_MSG = "empty assertion message";

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
}
