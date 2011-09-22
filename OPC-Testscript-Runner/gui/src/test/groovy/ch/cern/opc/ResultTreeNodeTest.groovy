package ch.cern.opc

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import groovy.xml.DOMBuilder

class ResultTreeNodeTest 
{
	def testee
	def xmlBuilder;
	def rootNode
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		rootNode = new RootNode()
	}
	
	@Test
	void testToString()
	{		
		testee = createNodeOnRoot('my red node', ResultTreeNodeColour.RED)
		assertEquals('my red node', testee.toString())
		assertEquals(testee.colour, ResultTreeNodeColour.RED)
	}
	
	@Test
	void testSuccessUpdateXmlMessageFromRunResultChangesTreeNodeToGreen()
	{
		testee = createNodeOnRoot('async assert run tree node', ResultTreeNodeColour.ORANGE)
		
		def xml = xmlBuilder.testcase(name:'async assert run tree node')
		{
			success(message:"I am asynchronous and I eventually passed")
		}
		
		testee.update(null, xml)
		sleep(1000)

		assertEquals(ResultTreeNodeColour.GREEN, testee.colour)
	}
	
	@Test
	void testFailureUpdateXmlMessageFromRunResultChangesTreeNodeToRed()
	{
		testee = createNodeOnRoot('async assert run tree node', ResultTreeNodeColour.ORANGE)
		
		def xml = xmlBuilder.testcase(name:'async assert run tree node')
		{
			failure(message:"I am asynchronous and I eventually failed")
		}
		
		testee.update(null, xml)
		sleep(1000)

		assertEquals(ResultTreeNodeColour.RED, testee.colour)
	}
	
	@Test
	void testSuccessUpdateXmlMessageFromRunResultChangesChildTreeNodeColourAndText()
	{
		testee = createNodeOnRoot('async assert run tree node', ResultTreeNodeColour.ORANGE)
		testee.add(new ResultTreeNode('message: incomplete, waiting for update', ResultTreeNodeColour.ORANGE))
		
		def xml = xmlBuilder.testcase(name:'async assert run tree node')
		{
			success(message:"I am asynchronous, I eventually passed")
		}
		
		testee.update(null, xml)
		sleep(1000)

		assertEquals(ResultTreeNodeColour.GREEN, testee.colour)
		assertEquals(ResultTreeNodeColour.GREEN, testee.getChildAt(0).colour)
		assertEquals(
			'message: I am asynchronous, I eventually passed', 
			testee.getChildAt(0).toString())
	}

	@Test
	void testFailureUpdateXmlMessageFromRunResultChangesChildTreeNodeColourAndText()
	{
		testee = createNodeOnRoot('async assert run tree node', ResultTreeNodeColour.ORANGE)
		testee.add(new ResultTreeNode('message: incomplete, waiting for update', ResultTreeNodeColour.ORANGE))
		
		def xml = xmlBuilder.testcase(name:'async assert run tree node')
		{
			failure(message:"I am asynchronous, I eventually failed")
		}
		
		testee.update(null, xml)
		sleep(1000)

		assertEquals(ResultTreeNodeColour.RED, testee.colour)
		assertEquals(ResultTreeNodeColour.RED, testee.getChildAt(0).colour)
		assertEquals(
			'message: I am asynchronous, I eventually failed',
			testee.getChildAt(0).toString())
	}
	
	def createNodeOnRoot(text, colour)
	{
		def node = new ResultTreeNode(text, colour)
		rootNode.add(node)
		
		return node
	}
}
