package ch.cern.opc.common;

import static org.junit.Assert.*;
import static ch.cern.opc.common.Quality.State.*;

import org.junit.Test;

import ch.cern.opc.common.Quality;

public class QualityTest 
{
	// OPC item quality is denoted by bits 7 and 8 of a 16 bit WORD
	private static final int QUALITY_BIT_LOW_POS = 6;
	private static final int QUALITY_BIT_HIGH_POS = 7;
	
	private final static Quality QUALITY_GOOD = new Quality(createQuality(true, true));
	private final static Quality QUALITY_BAD = new Quality(createQuality(false, false));
	private final static Quality QUALITY_UNCERTAIN = new Quality(createQuality(false, true));
	private final static Quality QUALITY_NA = new Quality(createQuality(true, false));

	@Test
	public void testQuality() 
	{
		assertEquals(QUALITY_BAD.state, BAD);
		assertEquals(QUALITY_GOOD.state, GOOD);
		assertEquals(QUALITY_UNCERTAIN.state, UNCERTAIN);
		assertEquals(QUALITY_NA.state, NA);
	}
	
	@Test
	public void testToString()
	{
		assertEquals("GOOD", QUALITY_GOOD.toString());
		assertEquals("BAD", QUALITY_BAD.toString());
		assertEquals("NA", QUALITY_NA.toString());
		assertEquals("UNCERTAIN", QUALITY_UNCERTAIN.toString());
	}
	
	@Test
	public void testEquals()
	{
		assertTrue(QUALITY_BAD.equals(BAD));
		assertTrue(QUALITY_GOOD.equals(GOOD));
		assertTrue(QUALITY_UNCERTAIN.equals(UNCERTAIN));
		assertTrue(QUALITY_NA.equals(NA));
		
		
	}
	
	private static int createQuality(final boolean qualityBitHigh, final boolean qualityBitLow)
	{
		int result = 0;
		
		if(qualityBitHigh) result |= (1 << QUALITY_BIT_HIGH_POS);
		if(qualityBitLow) result |= (1 << QUALITY_BIT_LOW_POS);
		
		return result;
	}

}
