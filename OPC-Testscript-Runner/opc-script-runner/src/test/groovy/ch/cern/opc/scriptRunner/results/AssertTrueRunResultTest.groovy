package ch.cern.opc.scriptRunner.results

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class AssertTrueRunResultTest 
{
	def xmlBuilder = DOMBuilder.newInstance()

	@Before
	void setup()
	{
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
	}
	
	@Test
	void testAssertTruePassing()
	{
		def testee = new AssertTrueRunResult('I should pass', true)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was true', success.'@message')
	}
	
	@Test
	void testAssertTrueFailing()
	{
		def testee = new AssertTrueRunResult('I should fail', false)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue failed: I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not true', failure.'@message')
	}

	@Test
	void testAssertTrueWithNull()
	{
		def testee = new AssertTrueRunResult('null input, I should fail', null)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue failed: null input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not true', failure.'@message')
	}
	
	@Test
	void testAssertTrueWithZero()
	{
		def testee = new AssertTrueRunResult('zero input, I should fail', 0)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue failed: zero input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not true', failure.'@message')
	}
	
	@Test
	void testAssertTrueWithOne()
	{
		def testee = new AssertTrueRunResult('one input, I should pass', 1)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue passed: one input, I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was true', success.'@message')
	}
	
	@Test
	void testAssertTrueWithStringTrue()
	{
		def testee = new AssertTrueRunResult('true input, I should pass', 'true')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue passed: true input, I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was true', success.'@message')
	}
	
	@Test
	void testAssertTrueWithStringNotTrue()
	{
		def testee = new AssertTrueRunResult('untrue input, I should fail', 'something other than the word true')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertTrue failed: untrue input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not true', failure.'@message')
	}
}
