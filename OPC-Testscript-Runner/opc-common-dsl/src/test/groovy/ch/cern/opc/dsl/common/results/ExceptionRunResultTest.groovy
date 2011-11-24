package ch.cern.opc.dsl.common.results;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before

import ch.cern.opc.dsl.common.results.ExceptionRunResult;

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

class ExceptionRunResultTest 
{
	static final def EXCEPTION_MSG = 'test exception'
	def xmlBuilder = DOMBuilder.newInstance()
	
	def testee
	
	@Before
	void setup()
	{
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
		
		testee = new ExceptionRunResult(new Exception(EXCEPTION_MSG))
	}
	
	@Test
	void testToXml()
	{
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals('Exception', xml.'@name')
		assertEquals(EXCEPTION_MSG, xml.'@message')
		
		assertTrue('should be multiple lines', xml.line.size() > 0)
	}
}
