package ch.cern.opc.dsl.common.results

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import org.apache.commons.lang.BooleanUtils

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import ch.cern.opc.dsl.common.results.AssertFalseRunResult;

class AssertFalseRunResultTest 
{
	def xmlBuilder = DOMBuilder.newInstance()
	
	@Before
	void setup()
	{
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
	}
	
	@Test
	void testAssertFalsePassing()
	{
		def testee = new AssertFalseRunResult('I should pass', false)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse passed: I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was false', success.'@message')
	}

	@Test
	void testAssertFalseFailing()
	{
		def testee = new AssertFalseRunResult('I should fail', true)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse failed: I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not false', failure.'@message')
	}

	@Test
	void testAssertTrueWithNull()
	{
		def testee = new AssertFalseRunResult('null input, I should fail', null)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse failed: null input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not false', failure.'@message')
	}
	
	@Test
	void testAssertTrueWithZero()
	{
		def testee = new AssertFalseRunResult('zero input, I should pass', 0)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse passed: zero input, I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was false', success.'@message')
	}

	@Test
	void testAssertTrueWithOne()
	{
		def testee = new AssertFalseRunResult('one input, I should fail', 1)
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse failed: one input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not false', failure.'@message')
	}
	
	@Test
	void testAssertTrueWithStringFalse()
	{
		def testee = new AssertFalseRunResult('false input, I should pass', 'false')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse passed: false input, I should pass', xml.'@name')
		
		def success = xml.success[0]
		assertEquals('value was false', success.'@message')
	}
	
	@Test
	void testAssertTrueWithStringNotFalse()
	{
		def testee = new AssertFalseRunResult('unfalse input, I should fail', 'something other than the word false')
		def xml = testee.toXml(xmlBuilder)
		
		assertEquals(1, xml.size())
		assertEquals('assertFalse failed: unfalse input, I should fail', xml.'@name')
		
		def failure = xml.failure[0]
		assertEquals('value was not false', failure.'@message')
	}
}
