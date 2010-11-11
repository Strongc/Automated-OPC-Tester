package ch.cern.opc

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import groovy.xml.DOMBuilder

class ResultsTreeTest 
{
	def testee
	def xml
	def nodeCounter
	
	static final def TEST_PASSED_MSG = 'well done you passed'
	static final def TEST_FAILED_MSG = 'you failed you insufferable fool'
	static final def EXCEPTION_MSG = 'some exception occurred'
	
	@Before
	void setup()
	{
		testee = new ResultsTree()
		generateXML()
		nodeCounter = 0
	}
	
	def generateXML()
	{
		def xmlBuilder = DOMBuilder.newInstance()
		xml = xmlBuilder.testsuites(name:'OPC Test Script Runner', tests:'2', failures:'1', disabled:'0', errors:'0', time:'0')
		{
			testsuite(name:'Tests', tests:'2', failures:'1', disabled:'0', errors:'0', time:'0')
			{
				testcase(name:'assertSomething')
				{
					success(message:TEST_PASSED_MSG)
				}
				testcase(name:'assertSomethingElse')
				{
					failure(message:TEST_FAILED_MSG)
				}
				exception(name:EXCEPTION_MSG)
				{
					line(line:'exception line 1')
					line(line:'exception line 2')
					line(line:'exception line 3')
				}
			}
		}
	}
	
	@Test
	void testAddResultsCreatesTreeNodes()
	{
		testee.addResults(xml)
		println testee
		assertEquals(10, treeNodeCount)
	}
	
	@Test
	void testClearTreeDiscardsEveythingUnderRoot()
	{
		testee.addResults(xml)
		testee.clearResults()
		assertEquals('root is left', 1, treeNodeCount)
	}
	
	private def getTreeNodeCount()
	{
		def result = 0
		
		def nodeCounterClosure
		nodeCounterClosure = 
		{node ->
			result++
			
			node.children.each
			{childNode->
				nodeCounterClosure(childNode)
			}
		}
		
		nodeCounterClosure(testee.root)
		
		return result
	}
}
