package ch.cern.opc.dsl.common.results;

import static RunResultUtil.*
//import static RunResultUtil.AnalyzedBooleanType.TRUE
//import static RunResultUtil.AnalyzedBooleanType.FALSE
import static org.junit.Assert.*
import org.junit.Test

class RunResultUtilTest 
{
	@Test
	void testToBoolean_straightBoolean()
	{
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean(true))
		assertEquals(RunResultUtil.AnalyzedBooleanType.FALSE, toBoolean(false))
	}
	
	@Test
	void testToBoolean_strings()
	{
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('true'))
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('True'))
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('TRUE'))
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('trUE'))
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('1'))
		assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean('-1'))
		
		// test a few postive random numbers
		for(i in 0..10)
		{ 
			assertEquals(RunResultUtil.AnalyzedBooleanType.TRUE, toBoolean(new Random().nextInt(99)+1))
		}
	}
	
}
