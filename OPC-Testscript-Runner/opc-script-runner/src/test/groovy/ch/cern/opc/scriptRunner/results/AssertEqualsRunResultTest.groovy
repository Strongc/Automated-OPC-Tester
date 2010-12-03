package ch.cern.opc.scriptRunner.results

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AssertEqualsRunResultTest 
{
	static final def TEST_LAST_ERR = 'this is the last error'
	
	def xmlBuilder
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
		
		def theClientInstance = [
				getLastError:{it->
					return TEST_LAST_ERR
				}
				] as ClientApi
		
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
	}
	
	@Test
	void testPassingAssertEquals()
	{
		def testee = new AssertEqualsRunResult('I should pass', 1.0, 1.0)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertEquals passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('expected [1.0] actual [1.0]', success.'@message')
	}
	
	@Test
	void testFailingAssertEquals()
	{
		def testee = new AssertEqualsRunResult('I should fail', 1.0, 2.0)
		def xml = testee.toXml(xmlBuilder)

		assertEquals(1, xml.size())
		assertEquals('assertEquals failed: I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		def expected = "expected [1.0] actual [2.0] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}
	
	@Test
	void testAssertEqualsWithExpectedAsNull()
	{
		def testee = new AssertEqualsRunResult('expected is NULL', null, 2.0)
		def xml = testee.toXml(xmlBuilder)

		assertEquals(1, xml.size())
		assertEquals('assertEquals failed: expected is NULL', xml.'@name')
		
		def failure = xml.failure[0]
		def expected = "expected [null] actual [2.0] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}
	
	@Test
	void testAssertEqualsWithActualAsNull()
	{
		def testee = new AssertEqualsRunResult('actual is NULL', 1.0, null)
		def xml = testee.toXml(xmlBuilder)

		assertEquals(1, xml.size())
		assertEquals('assertEquals failed: actual is NULL', xml.'@name')
		
		def failure = xml.failure[0]
		def expected = "expected [1.0] actual [null] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}
	
	@Test
	void testAssertEqualsWithExpectedAndActualAsNull()
	{
		def testee = new AssertEqualsRunResult('actual and expected are NULL', null, null)
		def xml = testee.toXml(xmlBuilder)

		assertEquals(1, xml.size())
		assertEquals('assertEquals passed: actual and expected are NULL', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('expected [null] actual [null]', success.'@message')
	}
}
