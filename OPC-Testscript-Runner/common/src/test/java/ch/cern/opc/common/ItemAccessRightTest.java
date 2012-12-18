package ch.cern.opc.common;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ItemAccessRightTest 
{

	@Test
	public void testFromId() 
	{
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromId(0));
		assertSame(ItemAccessRight.READ_ACCESS, ItemAccessRight.fromId(1));
		assertSame(ItemAccessRight.WRITE_ACCESS, ItemAccessRight.fromId(2));
		assertSame(ItemAccessRight.READ_WRITE_ACCESS, ItemAccessRight.fromId(3));
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromId(-1));
	}
	
	@Test
	public void testFromString() 
	{
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromString("UNKNOWN_ACCESS"));
		assertSame(ItemAccessRight.READ_ACCESS, ItemAccessRight.fromString("READ_ACCESS"));
		assertSame(ItemAccessRight.WRITE_ACCESS, ItemAccessRight.fromString("WRITE_ACCESS"));
		assertSame(ItemAccessRight.READ_WRITE_ACCESS, ItemAccessRight.fromString("READ_WRITE_ACCESS"));
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromString("I do not exist"));
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromString(""));
		assertSame(ItemAccessRight.UNKNOWN_ACCESS, ItemAccessRight.fromString(null));
	}
}
