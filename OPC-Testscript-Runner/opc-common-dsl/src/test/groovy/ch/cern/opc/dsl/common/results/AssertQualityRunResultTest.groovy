package ch.cern.opc.dsl.common.results

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import ch.cern.opc.common.Quality
import static ch.cern.opc.common.Quality.State.*
import ch.cern.opc.dsl.common.client.GenericClient
import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AssertQualityRunResultTest 
{
	static final def TEST_LAST_ERR = 'this is the last error'
	
	def xmlBuilder
	def client
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
		
		client = [getLastError:{it->return TEST_LAST_ERR}] as GenericClient
	}
	
	@Test
	void testPassingAssertGOODQuality()
	{
		def testee = new AssertQualityRunResult('I should pass', GOOD, new Quality(192), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertQuality passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('expected [GOOD] actual [GOOD]', success.'@message')
	}

	@Test
	void testPassingAssertBADQuality()
	{
		def testee = new AssertQualityRunResult('I should pass', BAD, new Quality(0), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertQuality passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('expected [BAD] actual [BAD]', success.'@message')
	}

	@Test
	void testFailingAssertQuality()
	{
		def testee = new AssertQualityRunResult('I should fail', GOOD, new Quality(0), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertQuality failed: I should fail', xml.'@name')

		def failure = xml.failure[0]
		def expected = "expected [GOOD] actual [BAD] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}
	
	@Test
	void testHandlesNullActualQuality()
	{
		def testee = new AssertQualityRunResult('I should fail, null actual quality given', GOOD, null, client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertQuality failed: I should fail, null actual quality given', xml.'@name')

		def failure = xml.failure[0]
		def expected = "expected [GOOD] actual [null] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}

	@Test
	void testHandlesNullExpectedQuality()
	{
		def testee = new AssertQualityRunResult('I should fail, null expected quality given', null, new Quality(192), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertQuality failed: I should fail, null expected quality given', xml.'@name')

		def failure = xml.failure[0]
		def expected = "expected [null] actual [GOOD] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expected, failure.'@message')
	}
}
