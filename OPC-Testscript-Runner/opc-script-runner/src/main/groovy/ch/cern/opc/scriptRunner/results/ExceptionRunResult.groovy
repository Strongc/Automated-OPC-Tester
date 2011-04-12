package ch.cern.opc.scriptRunner.results

protected class ExceptionRunResult extends ObservableRunResult
{
	static final def TITLE = 'Exception'
	private final def Exception e
	
	def ExceptionRunResult(Exception e)
	{
		this.e = e 
	}
	
	def toXml(xmlBuilder)
	{
		def element = xmlBuilder.exception(name:TITLE, message:"${e.message}")
		{
			e.stackTrace.each
			{
				line(line:"${it.toString()}")
			}
		}
		return element
	}
}
