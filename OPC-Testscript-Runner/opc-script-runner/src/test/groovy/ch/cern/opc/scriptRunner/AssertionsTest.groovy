package ch.cern.opc.scriptRunner

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import static ch.cern.opc.scriptRunner.Assertions.NULL_MSG
import static ch.cern.opc.scriptRunner.Assertions.EMPTY_MSG

class AssertionsTest 
{
	def testee
	
	@Before
	void setup()
	{
		testee = new Assertions()
	}
	
	@Test
	void testAssertTrue()
	{
		testee.assertTrue("should pass", true)
		
		assertTrue(testee.failures.empty)
		assertEquals(1, testee.passes.size)
		
		assertTrue(testee.passes[0].contains("assertTrue"))
		assertTrue(testee.passes[0].contains("should pass"))
	}
	
	@Test
	void testAssertTrueWithFalse()
	{
		testee.assertTrue("should fail", false)
		
		assertTrue(testee.passes.empty)
		assertEquals(1, testee.failures.size)
		
		assertTrue(testee.failures[0].contains("assertTrue"))
		assertTrue(testee.failures[0].contains("should fail"))
	}
	
	@Test
	void testAssertTrueFailureWithNullMessage()
	{
		testee.assertTrue(null, false)
		assertEquals(1, testee.failures.size)
		assertTrue(testee.failures[0].contains(NULL_MSG))
	}
	
	@Test
	void testAssertTrueFailureWithEmptyMessage()
	{
		testee.assertTrue(" ", false)
		assertEquals(1, testee.failures.size)
		assertTrue(testee.failures[0].contains(EMPTY_MSG))
	}
	
	@Test
	void testAssertEqualsForEqualValues()
	{
		testee.assertEquals("should pass", 1, 1)
		assertTrue(testee.failures.empty)
		
		assertTrue(testee.failures.empty)
		assertEquals(1, testee.passes.size)

		assertTrue(testee.passes[0].contains("assertEquals"))
		assertTrue(testee.passes[0].contains("should pass"))
		assertFalse(testee.passes[0].contains("expected [1] actual [2]"))
	}
	
	@Test
	void testAssertEqualsForNonEqualValues()
	{
		testee.assertEquals("should fail", 1, 2)
		assertEquals(1, testee.failures.size)
		assertTrue(testee.failures[0].contains("assertEquals"))
		assertTrue(testee.failures[0].contains("should fail"))
		assertTrue(testee.failures[0].contains("expected [1] actual [2]"))
	}
	
	@Test
	void testAssertMethodsStorePassesAndFailures()
	{
		testee.assertTrue(null, true)
		testee.assertTrue(null, false)
		testee.assertEquals(null, "a", "a")
		testee.assertEquals(null, "a", "b")
		
		assertEquals(2, testee.passes.size)
		assertEquals(2, testee.failures.size)
	}
	
	@Test
	void testGetAssertionsOutputInXmlFormat()
	{
		testee.assertTrue("a pass", true)
		testee.assertTrue("a fail", false)
		testee.assertEquals("another pass", 1, 1)
		testee.assertEquals("another fail", 1, 2)
/*		
		def expectedXml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<testsuites tests=\"2\" failures=\"1\" time=\"0.016\" name=\"OPC Test Script\">" +
			"<testsuite name=\"Script Name\" tests=\"2\" failures=\"1\" time=\"0.016\">" + 
				"<testcase name=\"assertTrue\" status=\"run\" time=\"0\" classname=\"Script Name\" />" +
				"<testcase name=\"assertTrue\" status=\"run\" time=\"0\" classname=\"Script Name\">" +
					"<failure message=\"assertTrue failed - message: a fail\"/>" +
				"</testcase>" + 
			"</testsuite>" + 
		"</testsuites>" 
*/
		
		println testee.XML
	}
	
	
}
