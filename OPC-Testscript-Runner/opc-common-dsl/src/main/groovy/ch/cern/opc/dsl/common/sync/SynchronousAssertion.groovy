package ch.cern.opc.dsl.common.sync

import ch.cern.opc.dsl.common.results.ObservableRunResult
import static ch.cern.opc.dsl.common.results.RunResultUtil.formatMessage

/**
 * Note SynchronousAssertion objects are immutable 
 * @author bfarnham
 *
 */
final class SynchronousAssertion extends ObservableRunResult
{
	public final def title
	public final boolean isPassed
	public final String userMessage
	public final String passFailMessage

	def SynchronousAssertion(title, isPassed, userMessage, passFailMessage)
	{
		if(title == null || title.isEmpty()) throw new IllegalArgumentException('assertion type must be provided')
		
		this.title = title
		this.isPassed = isPassed
		this.userMessage = formatMessage(userMessage).toString()
		this.passFailMessage = formatMessage(passFailMessage).toString()
	}
	
	@Override
	def toXml(xmlBuilder)
	{
		def element
		
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"${title} passed: ${userMessage}")
			{
				success(message:passFailMessage)
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"${title} failed: ${userMessage}")
			{
				failure(message:passFailMessage)
			}
		}
		
		return element
	}
}
