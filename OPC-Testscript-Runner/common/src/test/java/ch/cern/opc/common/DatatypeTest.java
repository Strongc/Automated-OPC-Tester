package ch.cern.opc.common;

import static org.junit.Assert.*;
import static ch.cern.opc.common.Datatype.*;

import org.junit.Test;

public class DatatypeTest 
{

	@Test
	public void testTypeMappingById() 
	{
		assertEquals(VT_UNRECOGNISED, getDatatypeById(-1));
		assertEquals(VT_UNRECOGNISED, getDatatypeById(123456));
		
		assertEquals(VT_EMPTY, getDatatypeById(0));
		assertEquals(VT_I2, getDatatypeById(2));
		assertEquals(VT_I4, getDatatypeById(3));
		assertEquals(VT_R4, getDatatypeById(4));
		assertEquals(VT_R8, getDatatypeById(5));
		assertEquals(VT_CY, getDatatypeById(6));
		assertEquals(VT_DATE, getDatatypeById(7));
		assertEquals(VT_BSTR, getDatatypeById(8));
		assertEquals(VT_BOOL, getDatatypeById(11));
	}
	
	@Test
	public void testTypeMappingByName() 
	{
		assertEquals(VT_UNRECOGNISED, getDatatypeByName(null));
		assertEquals(VT_UNRECOGNISED, getDatatypeByName(""));
		assertEquals(VT_UNRECOGNISED, getDatatypeByName("invalid name"));
		
		assertEquals(VT_EMPTY, getDatatypeByName("VT_EMPTY"));
		assertEquals(VT_I2, getDatatypeByName("VT_I2"));
		assertEquals(VT_I4, getDatatypeByName("VT_I4"));
		assertEquals(VT_R4, getDatatypeByName("VT_R4"));
		assertEquals(VT_R8, getDatatypeByName("VT_R8"));
		assertEquals(VT_CY, getDatatypeByName("VT_CY"));
		assertEquals(VT_DATE, getDatatypeByName("VT_DATE"));
		assertEquals(VT_BSTR, getDatatypeByName("VT_BSTR"));
		assertEquals(VT_BOOL, getDatatypeByName("VT_BOOL"));
		assertEquals(VT_UNRECOGNISED, getDatatypeByName("VT_UNRECOGNISED"));
		
	}
	
}
