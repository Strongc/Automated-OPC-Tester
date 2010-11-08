package ch.cern.opc

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import groovy.xml.DOMBuilder
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

class ResultsTreeTest 
{
	def testee
	def xml
	def nodeCounter
	
	static final def TEST_PASSED_MSG = 'well done you passed'
	static final def TEST_FAILED_MSG = 'you failed you insufferable fool'
	
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
				testcase(name:'assertSomething, passed')
				{
					success(message:TEST_PASSED_MSG)
				}
				testcase(name:'assertSomethingElse, failure')
				{
					failure(message:TEST_FAILED_MSG)
				}
				exception(message:'some exception message')
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
		println xml
		testee.addResults(xml)
		assertEquals(10, countTreeNodes(testee.root))
	}
	
	@Test
	void testPassedTestNodesHaveCorrectMessage()
	{
		testee.addResults(xml)
		
		def passedNodes = []
		getPassedTestNodes(testee.root, passedNodes)

		assertEquals(1, passedNodes.size)
		assertEquals(TEST_PASSED_MSG, passedNodes[0].getChildAt(0).toString())
	}
	
	@Test
	void testFailedTestNodesHaveCorrectMessage()
	{
		testee.addResults(xml)
		
		def failedNodes = []
		getFailedTestNodes(testee.root, failedNodes)
		
		assertEquals(1, failedNodes.size)
		assertEquals(TEST_FAILED_MSG, failedNodes[0].getChildAt(0).toString())
	}
	
	@Test
	void testClearTreeDiscardsEveythingUnderRoot()
	{
		testee.addResults(xml)
		testee.clearResults()
		assertEquals(0, countTreeNodes(testee.root))
	}
	
	private def countTreeNodes(TreeNode node)
	{
		nodeCounter += node.childCount
		
		node.children.each{
			countTreeNodes(it)
		}

		return nodeCounter
	}
	
	private def getPassedTestNodes(TreeNode node, nodes)
	{
		/*
		 * test passed tree node structure is
		 * +assertSomething
		 * 	|_test passed
		 */
		if(node.childCount == 1 && TEST_PASSED_MSG.equals(node.getChildAt(0).toString()))
		{
			nodes << node
		}
		
		node.children.each{
			getPassedTestNodes(it, nodes)
		}
	}
	
	private def getFailedTestNodes(TreeNode node, nodes)
	{
		/*
		 * test passed tree node structure is
		 * +assertSomething
		 * 	|_failed: failure message...
		 */
		if(node.childCount == 1 && TEST_FAILED_MSG.equals(node.getChildAt(0).toString()))
		{
			nodes << node
		}
		
		node.children.each{
			getFailedTestNodes(it, nodes)
		}
	}
	
}
