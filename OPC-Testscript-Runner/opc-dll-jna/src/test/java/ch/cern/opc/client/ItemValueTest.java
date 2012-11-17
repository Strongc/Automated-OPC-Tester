package ch.cern.opc.client;


import static ch.cern.opc.common.Quality.State.BAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class ItemValueTest 
{
	IllegalArgumentException e;
	ItemValue testee;
	
	private static final String VALUE = "69";
	private static final String TIMESTAMP = "Some timestamp";
	private static final int QUALITY = 0;
	private static final int DATATYPE = 0;  
	
	@Before
	public void setup()
	{
		testee = new ItemValue(VALUE, QUALITY, TIMESTAMP, DATATYPE);
	}
	
	@Test
	public void testCtor()
	{		
		testee = new ItemValue(VALUE, QUALITY, TIMESTAMP, DATATYPE);
		
		assertEquals(VALUE, testee.value);
		assertTrue(testee.quality.state.equals(BAD));
		assertEquals(TIMESTAMP, testee.timestamp);
		assertEquals(DATATYPE, testee.datatype);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCtorRejectsNullValueParameter()
	{
		testee = new ItemValue(null, QUALITY, TIMESTAMP, DATATYPE);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCtorRejectsNullTimestampParameter()
	{
		testee = new ItemValue(VALUE, QUALITY, null, DATATYPE);
	}
}
