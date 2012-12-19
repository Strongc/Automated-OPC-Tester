package ch.cern.opc.dsl.common.sync

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SynchronousAssertionTest 
{
	private def xmlBuilder
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
	}
	
	@Test
	void testXmlConstructedCorrectlyForPassed()
	{
		def testee = new SynchronousAssertion('theAssertionTypeName', true, 'some message from the user code', 'the pass fail message')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('theAssertionTypeName passed: some message from the user code', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('the pass fail message', success.'@message')
	}
	
	@Test
	void testXmlConstructedCorrectlyForFailed()
	{
		def testee = new SynchronousAssertion('theAssertionTypeName', false, 'some message from the user code', 'the pass fail message')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('theAssertionTypeName failed: some message from the user code', xml.'@name')
		
		def success = xml.failure[0]
		assertEquals('the pass fail message', success.'@message')
	}
	
	@Test
	void testXmlConstructedCorrectlyForMissingUserMessages()
	{
		def testeeWithEmptyMessage = new SynchronousAssertion('theAssertionTypeName', true, '', 'the pass fail message')
		assertTrue(testeeWithEmptyMessage.toXml(xmlBuilder).'@name'.contains('empty assertion message'))
		
		def testeeWithNullMessage = new SynchronousAssertion('theAssertionTypeName', false, null, 'the pass fail message')
		assertTrue(testeeWithNullMessage.toXml(xmlBuilder).'@name'.contains('null assertion message'))
	}
	
	@Test
	void testXmlConstructedCorrectlyForMissingPassFailMessages()
	{
		def testeeWithEmptyMessage = new SynchronousAssertion('theAssertionTypeName', true, 'user message', '')
		assertTrue(testeeWithEmptyMessage.toXml(xmlBuilder).success[0].'@message'.contains('empty assertion message'))
		
		def testeeWithNullMessage = new SynchronousAssertion('theAssertionTypeName', false, 'user message', null)
		assertTrue(testeeWithNullMessage.toXml(xmlBuilder).failure[0].'@message'.contains('null assertion message'))
	}
	
	@Test(expected=IllegalArgumentException.class)
	void testConstructorThrowsExceptionForNullAssertionType()
	{
		new SynchronousAssertion(null, true, 'user message', 'the pass fail message')
	}

	
	@Test(expected=IllegalArgumentException.class)
	void testConstructorThrowsExceptionForEmptyAssertionType()
	{
		new SynchronousAssertion('', true, 'user message', 'the pass fail message')
	}
}
