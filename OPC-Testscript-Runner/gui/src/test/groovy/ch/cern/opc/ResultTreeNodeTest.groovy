package ch.cern.opc

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import groovy.xml.DOMBuilder

class ResultTreeNodeTest 
{
	def testee
	def xmlBuilder;
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
	}
	
	@Test
	void testToString()
	{
		testee = new ResultTreeNode('my red node', ResultTreeNodeColour.RED)
		assertEquals('my red node', testee.toString())
		assertEquals(testee.colour, ResultTreeNodeColour.RED)
	}
}
