package ch.cern.opc.scriptRunner.results

import static org.junit.Assert.*
import org.apache.commons.lang.NotImplementedException
import org.junit.Test
import org.junit.Before

class ObservableRunResultTest 
{
	private def testee
	
	@Before
	void setup()
	{
		testee = new NonAbstractObservableRunResult()
	}
	
	@Test
	void testIsObservable()
	{
		def observer = {throw NotImplementedException('I aint got nobody...')} as Observer
		
		try
		{
			testee.addObserver(observer)
		}
		catch(MissingMethodException e)
		{
			fail('testee should be Observable')
		}
	}
	
	class NonAbstractObservableRunResult extends ObservableRunResult
	{
		@Override
		def toXml(xmlBuilder)
		{
			throw new NotImplementedException('I aint got nobody...')
		}
	}
}
