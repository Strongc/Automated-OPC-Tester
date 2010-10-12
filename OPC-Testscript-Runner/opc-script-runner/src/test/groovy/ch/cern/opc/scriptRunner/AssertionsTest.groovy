package ch.cern.opc.scriptRunner

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static ch.cern.opc.scriptRunner.Assertions.NULL_MSG
import static ch.cern.opc.scriptRunner.Assertions.EMPTY_MSG

class AssertionsTest 
{
	static final def TEST_LAST_ERR = 'this is the last error'

	def testee
	
	@Before
	void setup()
	{
		def theClientInstance = [
			getLastError:{it->
				return TEST_LAST_ERR
			}
		] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
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

		def result =  testee.XML
		println(result)
		
		use(DOMCategory)
		{
			assertEquals(1, result.size())
			
			// <testsuites> (root element)
			assertEquals('OPC Test Script Runner', result.'@name')
			assertEquals(4, result.'@tests'.toInteger())
			assertEquals(2, result.'@failures'.toInteger())
			assertEquals(1, result.testsuite.size())

			// <testsuite>
			def testsuite = result.testsuite[0]
			assertEquals('Tests', testsuite.'@name')
			assertEquals(4, testsuite.'@tests'.toInteger())
			assertEquals(2, testsuite.'@failures'.toInteger())
			assertEquals(4, testsuite.size())
			
			// <testcase>
			def testcases = testsuite.'testcase'
			assertEquals(2, testcases.findAll{it.'@name'.contains('assertTrue')}.size())
			assertEquals(2, testcases.findAll{it.'@name'.contains('assertEquals')}.size())
			def failedTestcases = testcases.findAll{it.failure.size() > 0} 
			assertEquals(2, failedTestcases.size())
			
			// failedTestCases are <testcase> with <failure> children. How very sad.
			def assertTrueFailure = failedTestcases.findAll{it.'@name'.contains('assertTrue')}[0]
			def assertEqualsFailure = failedTestcases.findAll{it.'@name'.contains('assertEquals')}[0]
			assertEquals('assertTrue failed - message: a fail', assertTrueFailure.'@name')
			assertEquals('assertEquals failed - message: another fail expected [1] actual [2]', assertEqualsFailure.'@name')
			failedTestcases.each {
				assertEquals(1, it.failure.size())
				//assertEquals('failed', it.failure[0].'@message')
				assertTrue(it.failure[0].'@message'.contains(TEST_LAST_ERR)) 
			}
		}		
	}
	
	
}
