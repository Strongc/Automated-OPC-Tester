package ch.cern.opc

class ConsoleOutputRedirection 
{
	static def textArea = null
	
	def static println(self, text)
	{
		if(textArea == null)
		{
			return
		}
		
		if(textArea.text != null && textArea.text.size() > 0)
		{
			textArea.append("\n${text}")
		}
		else
		{
			textArea.append(text)
		}
	}
}
