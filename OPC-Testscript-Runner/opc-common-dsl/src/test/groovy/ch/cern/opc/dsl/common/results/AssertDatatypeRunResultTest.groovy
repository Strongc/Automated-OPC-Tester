package ch.cern.opc.dsl.common.results

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import ch.cern.opc.common.Quality
import static ch.cern.opc.common.Datatype.*
import ch.cern.opc.dsl.common.client.GenericClient
import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AssertDatatypeRunResultTest 
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
	void testPassingAssertMatchingDatatype()
	{
		def testee = new AssertDatatypeRunResult('I should pass', VT_BOOL, getDatatypeByName('VT_BOOL'), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertDatatype passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('expected [VT_BOOL] actual [VT_BOOL]', success.'@message')
	}

	@Test
	void testFailingAssertNonMatchingDatatype()
	{
		def testee = new AssertDatatypeRunResult('I should fail', VT_BSTR, getDatatypeByName('VT_BOOL'), client)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertDatatype failed: I should fail', xml.'@name')
		
		def success = xml.failure[0]
		def expectedString = "expected [VT_BSTR] actual [VT_BOOL] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expectedString, success.'@message')
	}
	
	
	@Test
	void testFailingAssertNullActualDatatype()
	{
		def testee = new AssertDatatypeRunResult('I should fail', VT_BSTR, null, client)
		def xml = testee.toXml(xmlBuilder)
		
		def success = xml.failure[0]

		def expectedString = "expected [VT_BSTR] actual [null] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expectedString, success.'@message')
	}
	
	@Test
	void testFailingAssertNullExpectedDatatype()
	{
		def testee = new AssertDatatypeRunResult('I should fail', null, VT_BSTR, client)
		def xml = testee.toXml(xmlBuilder)
		
		def success = xml.failure[0]

		def expectedString = "expected [null] actual [VT_BSTR] last error from dll [${TEST_LAST_ERR}]".toString()
		assertEquals(expectedString, success.'@message')
	}
}
