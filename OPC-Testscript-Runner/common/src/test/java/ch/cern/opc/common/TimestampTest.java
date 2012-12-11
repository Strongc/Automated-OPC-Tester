package ch.cern.opc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class TimestampTest 
{
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/M/d-H:m:s.SSS");
	
	private final static String TIME_STRING = "2012/11/19-18:48:2.411";
	private final static Date TIME_VALUE = asDate(TIME_STRING);
	
	private Timestamp testee;
	
	@Before
	public void setup()
	{
		testee = new Timestamp(TIME_STRING);
	}

	@Test
	public void testToString() 
	{
		assertEquals(TIME_STRING, testee.toString());
	}
	
	@Test
	public void testToDate()
	{
		assertEquals(TIME_VALUE, testee.toDate());
	}
	
	@Test
	public void testToDateHandlesInvalidDates()
	{
		assertNull(new Timestamp((Date)null).toDate());
		assertNull(new Timestamp((String)null).toDate());
		assertNull(new Timestamp("").toDate());
		assertNull(new Timestamp(" ").toDate());
		assertNull(new Timestamp("pish").toDate());
		assertNull(new Timestamp("000/00/0/00/00/0/0").toDate());
	}
	
	@Test
	public void testIsAfter()
	{
		Date after = DateUtils.addMinutes(TIME_VALUE, 3);
		Date before = DateUtils.addMinutes(TIME_VALUE, -3);
		
		// Date type
		assertFalse(testee.isAfter(after));
		assertTrue(testee.isAfter(before));
		
		// String type
		assertFalse(testee.isAfter(DATE_FORMAT.format(after)));
		assertTrue(testee.isAfter(DATE_FORMAT.format(before)));
		
		// Timestamp type
		assertFalse(testee.isAfter(new Timestamp(DATE_FORMAT.format(after))));
		assertTrue(testee.isAfter(new Timestamp(DATE_FORMAT.format(before))));
		
		// junk
		try
		{
			assertFalse(testee.isAfter((Date)null));
			assertFalse(testee.isAfter((String)null));
			assertFalse(testee.isAfter("Not a date"));
			assertFalse(testee.isAfter((Timestamp)null));
			assertFalse(testee.isAfter(new Timestamp((Date)null)));
			assertFalse(testee.isAfter(new Timestamp("Not a date")));
		}
		catch(Exception e)
		{
			fail("cannot handle junk");
		}
	}
	
	@Test
	public void testIsBefore()
	{
		Date before = DateUtils.addMinutes(TIME_VALUE, -3);
		Date after = DateUtils.addMinutes(TIME_VALUE, 3);

		// Date type
		assertFalse(testee.isBefore(before));
		assertTrue(testee.isBefore(after));
		
		// String type
		assertFalse(testee.isBefore(DATE_FORMAT.format(before)));
		assertTrue(testee.isBefore(DATE_FORMAT.format(after)));

		// Timestamp type
		assertFalse(testee.isBefore(new Timestamp(DATE_FORMAT.format(before))));
		assertTrue(testee.isBefore(new Timestamp(DATE_FORMAT.format(after))));

		// junk
		try
		{
			assertFalse(testee.isBefore((Date)null));
			assertFalse(testee.isBefore((String)null));
			assertFalse(testee.isBefore("Not a date"));
			assertFalse(testee.isBefore((Timestamp)null));
			assertFalse(testee.isBefore(new Timestamp((Date)null)));
			assertFalse(testee.isBefore(new Timestamp("Not a date")));
		}
		catch(Exception e)
		{
			fail("cannot handle junk");
		}
	}
	
	private static Date asDate(final String dateString)
	{
		try 
		{
			return DATE_FORMAT.parse(dateString);
		} 
		catch (ParseException e) 
		{
			fail("Failed to parse expected time string ["+TIME_STRING+"]");
			return null;
		}
	}

}
