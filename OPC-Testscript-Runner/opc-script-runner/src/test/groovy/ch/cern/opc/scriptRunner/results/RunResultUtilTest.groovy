package ch.cern.opc.scriptRunner.results;

import static RunResultUtil.*
import static RunResultUtil.AnalyzedBooleanType.*
import static org.junit.Assert.*
import org.junit.Test

class RunResultUtilTest 
{
	@Test
	void testToBoolean_straightBoolean()
	{
		assertEquals(TRUE, toBoolean(true))
		assertEquals(FALSE, toBoolean(false))
	}
	
	@Test
	void testToBoolean_strings()
	{
		assertEquals(TRUE, toBoolean('true'))
		assertEquals(TRUE, toBoolean('True'))
		assertEquals(TRUE, toBoolean('TRUE'))
		assertEquals(TRUE, toBoolean('trUE'))
		assertEquals(TRUE, toBoolean('1'))
		assertEquals(TRUE, toBoolean('-1'))
		
		// test a few postive random numbers
		for(i in 0..10)
		{ 
			assertEquals(TRUE, toBoolean(new Random().nextInt(99)+1))
		}
	}
	
}
