package ch.cern.opc.ua.dsl

import org.opcfoundation.ua.builtintypes.*
import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class ValueWrapperTest 
{
	private final static def SRC_TIME = new DateTime()
	private final static def SVR_TIME = new DateTime()
	
	private final static def INT_DV = new DataValue(
		new Variant(Integer.valueOf(1)),
		StatusCode.GOOD,
		SRC_TIME,
		SVR_TIME)
	
	private final static def STR_DV = new DataValue(
		new Variant("a.string.value"),
		StatusCode.GOOD,
		SRC_TIME,
		SVR_TIME)
	
	private final static def BOL_DV = new DataValue(
		new Variant(Boolean.FALSE),
		StatusCode.GOOD,
		SRC_TIME,
		SVR_TIME)
	
	private final static def FLT_DV = new DataValue(
		new Variant(Float.valueOf(12.345)),
		StatusCode.GOOD,
		SRC_TIME,
		SVR_TIME)
	
	
	
	@Before
	void setup()
	{
		
	}
	
	@Test
	void testIntDataValue()
	{
		def testee = new ValueWrapper(INT_DV)
		
		assertEquals('1', testee.value)
		assertEquals('Integer', testee.type)
		assertEquals(SRC_TIME.toString(), testee.sourceTimestamp)
		assertEquals(SVR_TIME.toString(), testee.serverTimestamp)
	}
	
	@Test
	void testStringDataValue()
	{
		def testee = new ValueWrapper(STR_DV)
		
		assertEquals('a.string.value', testee.value)
		assertEquals('String', testee.type)
		assertEquals(SRC_TIME.toString(), testee.sourceTimestamp)
		assertEquals(SVR_TIME.toString(), testee.serverTimestamp)
	}
	
	@Test
	void testBooleanDataValue()
	{
		def testee = new ValueWrapper(BOL_DV)
		
		assertEquals('false', testee.value)
		assertEquals('Boolean', testee.type)
	}
	
	@Test
	void testFloatDataValue()
	{
		def testee = new ValueWrapper(FLT_DV)
		
		assertEquals('12.345', testee.value)
		assertEquals('Float', testee.type)
	}
	
	@Test
	void testNullDataValue()
	{
		def testee = new ValueWrapper(null)
		
		assertEquals('null', testee.value)
		assertEquals('null', testee.type)
		assertEquals('null', testee.sourceTimestamp)
		assertEquals('null', testee.serverTimestamp)
	}
	
	@Test
	void testToStringReturnsValue()
	{
		def testee = new ValueWrapper(BOL_DV)
		assertEquals(testee.value, testee.toString())
	}
	
	

}
