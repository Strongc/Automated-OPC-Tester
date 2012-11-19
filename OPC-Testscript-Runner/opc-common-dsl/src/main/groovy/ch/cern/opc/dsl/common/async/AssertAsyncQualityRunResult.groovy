package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.ItemValue
import ch.cern.opc.common.Quality
import ch.cern.opc.common.Quality.State
import static ch.cern.opc.common.Quality.State.*
import static ch.cern.opc.dsl.common.async.AsyncState.*
import static ch.cern.opc.dsl.common.results.RunResultUtil.formatMessage

class AssertAsyncQualityRunResult extends AssertAsyncRunResult 
{
	public static final def TITLE = 'assertAsyncQuality'
	final def message
	
	private ItemValue theFailedUpdate = null
	
	public AssertAsyncQualityRunResult(message, final timeout, final itemPath, final Quality.State expectedQuality)
	{
		super(timeout, itemPath, expectedQuality)
		this.message = formatMessage(message)
	}

	@Override
	def checkUpdate(Object itemPath, final ItemValue actualValue) 
	{
		if(isItemPathMatch(itemPath))
		{
			if(!actualValue.quality.equals(itemValue))
			{
				theFailedUpdate = actualValue
				state = FAILED
			}
		}
	}

	@Override
	def timedOut() 
	{
		state = PASSED
	}

	@Override
	def toXml(Object xmlBuilder) 
	{
		def element
		
		switch(state)
		{
			case FAILED:
				element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
				{
					failure(message:"item [${itemPath}] expected quality was [${itemValue}] received unexpected quality [${failureQuality}] at [${failureTimestamp}]. Elapsed wait [${elapsedWait}] seconds")
				}
				break;
			case PASSED:
				element = xmlBuilder.testcase(name:"${TITLE} success: ${message}")
				{
					success(message:"item [${itemPath}] received no quality updates contrary to expected quality [${itemValue}] in [${elapsedWait}] seconds")
				}
				break;
			case WAITING:
				element = xmlBuilder.testcase(name:"${TITLE} incomplete: ${message}")
				{
					incomplete(message:"item [${itemPath}] still waiting, no quality updates contrary to expected quality [${itemValue}] have been received, elapsed wait [${elapsedWait}] seconds")
				}
				break;
			default:
				throw new IllegalStateException("programming error: asynchronous assertions should not be asked for their XML in current state [${state}]")
		}
		return element
	}
	
	private String getFailureQuality()
	{
		return (theFailedUpdate != null? theFailedUpdate.quality.toString(): 'NULL QUALITY')
	}
	
	private String getFailureTimestamp()
	{
		return (theFailedUpdate != null? theFailedUpdate.timestamp.toString(): 'NULL TIMESTAMP')
	}
}
