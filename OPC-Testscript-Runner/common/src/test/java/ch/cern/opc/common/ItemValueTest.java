package ch.cern.opc.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;



public class ItemValueTest 
{
	IllegalArgumentException e;
	ItemValue testee;
	
	private static final String VALUE = "69";
	private static final String TIMESTAMP = "Some timestamp";
	private static final int QUALITY = 192;
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
		assertTrue(testee.quality.equals(Quality.State.GOOD));
		assertEquals(TIMESTAMP, testee.timestamp);
		assertEquals(DATATYPE, testee.datatype);
	}
	
	@Test
	public void testEquals()
	{
		assertTrue(testee.equals(testee));
		assertTrue(testee.equals(new ItemValue(VALUE, QUALITY, TIMESTAMP, DATATYPE)));
		
		assertFalse(testee.equals(null));
		assertFalse(testee.equals(new Object()));
		assertFalse(testee.equals(new ItemValue("other value", QUALITY, TIMESTAMP, DATATYPE)));
		assertFalse(testee.equals(new ItemValue(VALUE, 0, TIMESTAMP, DATATYPE)));
		assertFalse(testee.equals(new ItemValue(VALUE, QUALITY, TIMESTAMP+"xx", DATATYPE)));
		assertFalse(testee.equals(new ItemValue(VALUE, QUALITY, TIMESTAMP, DATATYPE+1)));
	}
	
	@Test
	public void testCtorHandlesNullValueParameter()
	{
		testee = new ItemValue(null, QUALITY, TIMESTAMP, DATATYPE);
		assertEquals("", testee.value);
	}
	
	@Test
	public void testCtorHandlesNullTimestampParameter()
	{
		testee = new ItemValue(VALUE, QUALITY, null, DATATYPE);
		assertEquals("", testee.timestamp);
	}
}
