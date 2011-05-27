package ch.cern.opc

import ch.cern.opc.scriptRunner.results.RunResult

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import groovy.xml.DOMBuilder

import java.util.regex.Matcher
import java.util.regex.Pattern


class TreeNodeFactoryTest 
{
	private testee
	private xmlBuilder
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		testee = new TreeNodeFactory()
	}
	
	@Test
	void testCreateNode_CreatesTestcaseNodeForAssertTrueRunResult()
	{
		def xml = xmlBuilder.testcase(name:"some nm")
		{
			success(message:"some msg")
		}
		
		def runResult = {Object[] args -> return xml} as RunResult
		def node = testee.createNode(runResult)
		
		assertEquals('some nm', node.toString())
		assertEquals(ResultTreeNodeColour.GREEN, node.colour)
		
		def childNode = node.getChildAt(0)
		assertEquals('message: some msg', childNode.toString())
	}
	
	@Test
	void testCreateNode_ForSuccessfulTestcase()
	{
		def xml = xmlBuilder.testcase(name:'someAssertionType')
		{
			success(message:'well done, you passed')
		}
		def runResult = {Object[] args -> return xml} as RunResult
		def node = testee.createNode(runResult)
		
		assertEquals('someAssertionType', node.toString())
		assertEquals(ResultTreeNodeColour.GREEN, node.colour)
		
		def childNode = node.getChildAt(0)
		assertEquals('message: well done, you passed', childNode.toString())
	}
	
	@Test
	void testCreateTestcaseNodeForFailedTestcase()
	{
		def xml = xmlBuilder.testcase(name:'thisAssertionFails')
		{
			failure(message:'fool, you failed')
		}
		
		def runResult = {Object[] args -> return xml} as RunResult
		def node = testee.createNode(runResult)
		
		assertEquals('thisAssertionFails', node.toString())
		assertEquals(ResultTreeNodeColour.RED, node.colour)
		
		def childNode = node.getChildAt(0)
		
		assertEquals('message: fool, you failed', childNode.toString())
		assertEquals(ResultTreeNodeColour.RED, childNode.colour)
	}
	
	@Test(expected = IllegalArgumentException.class)
	void testCreateTestcaseNodeForNonTestCaseElement()
	{
		def xml = xmlBuilder.someNonTestCase()
		def runResult = {Object[] args -> return xml} as RunResult
		
		testee.createNode(runResult)
	}
	
	@Test(expected = IllegalStateException.class)
	void testCreateTestcaseNodeForElementWithNoChildren()
	{
		def xml = xmlBuilder.testcase()
		def runResult = {Object[] args -> return xml} as RunResult
		
		testee.createNode(runResult)
	}
	
	@Test(expected = IllegalStateException.class)
	void testCreateTestcaseNodeForElementWithInvalidChildren()
	{
		def xml = xmlBuilder.testcase()
		{
			invalidChild()	
		}
		def runResult = {Object[] args -> return xml} as RunResult

		testee.createNode(runResult)
	}
	
	@Test
	void testCreateExceptionNodeWithExceptionDetail()
	{
		def xml = xmlBuilder.exception(message:'aaargh')
		{
			line(line:'stack trace line 1')
			line(line:'stack trace line 2')
			line(line:'stack trace line 3')
		}
		def runResult = {Object[] args -> return xml} as RunResult
		
		def node = testee.createNode(runResult)
		
		assertEquals('exception message: aaargh', node.toString())
		assertEquals(ResultTreeNodeColour.RED, node.colour)
		assertEquals(3, node.childCount)
	}
	
	@Test(expected = IllegalArgumentException.class)
	void testCreateExceptionNodeWithInvalidArgument()
	{
		def xml = xmlBuilder.notAnExceptionElement()
		def runResult = {Object[] args -> return xml} as RunResult
		
		testee.createNode(runResult)
	}
	
	@Test
	void testCreateTestsuiteNode()
	{
		def node = testee.createTestsuiteNode()
		
		assertEquals('testsuite', node.toString())
		assertTrue(node.leaf)
	}
	
	@Test(expected = IllegalArgumentException.class)
	void testCreateExceptionNodeWithInvalidChildren()
	{
		def xml = xmlBuilder.exception(message:'barf')
		{
			anythingExceptLine()
		}
		def runResult = {Object[] args -> return xml} as RunResult
		
		testee.createNode(runResult)
	}
	
	@Test
	void testCreateNodes()
	{
		def xml = xmlBuilder.testsuites(name:'OPC Test Script Runner', tests:'2', failures:'1', disabled:'0', errors:'0', time:'0')
		{
			testsuite(name:'Tests', tests:'2', failures:'1', disabled:'0', errors:'0', time:'0')
			{
				testcase(name:'assertSomething, passed')
				{
					success(message:'good')
				}
				testcase(name:'assertSomethingElse, failure')
				{
					failure(message:'bad')
				}
				exception(message:'baaarf')
				{
					line(line:'exception line 1')
					line(line:'exception line 2')
					line(line:'exception line 3')
				}
			}
		}
		
		def result = testee.createNodes(xml)
		
		assertEquals(1, getTreeNodeCountWithPattern(result, 'testsuites'))
		assertEquals(1, getTreeNodeCountWithPattern(result, 'testsuite'))
		assertEquals(1, getTreeNodeCountWithPattern(result, 'assertSomething, passed'))
		assertEquals(1, getTreeNodeCountWithPattern(result, 'assertSomethingElse, failure'))
		assertEquals(1, getTreeNodeCountWithPattern(result, 'message: bad'))
		assertEquals(1, getTreeNodeCountWithPattern(result, 'exception message: [a-zA-Z\\ ]*'))
		assertEquals(3, getTreeNodeCountWithPattern(result, 'exception line [0-9]'))
	}
	
	@Test
	void testCreateTestcaseNodeForIncompleteAsyncAssert()
	{
		def xml = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			incomplete(message:'some user message for incomplete asynchronous test')	
		}
		
		def runResult = {Object[] args -> return xml} as RunResult
		
		def node = testee.createNode(runResult)
		println(node)
		assertEquals('assertAsyncNotEquals', node.toString())
		assertEquals(ResultTreeNodeColour.ORANGE, node.colour)
		assertEquals(1, node.childCount)
	}
	
	@Test
	void testRegexp()
	{
		def patternText = '[a-zA-Z\\ ]*end_of_line'
		def pattern = ~/${patternText}/
		
		assertTrue(pattern.matcher('a a a end_of_line').matches())
	}
	
	@Test
	void testUpdateNode_ChangesColourFromOrangeToGreenForSuccessfulTest()
	{
		def node = createTreeNodeForIncompleteAsyncTest()
		
		def successXml = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			success(message:'yay - the async text completed after some time.')	
		}
		
		testee.updateNode(node, successXml)
		
		assertEquals(ResultTreeNodeColour.GREEN, node.colour)
		assertEquals(ResultTreeNodeColour.GREEN, node.getChildAt(0).colour)
	}
	
	@Test
	void testUpdateNode_ChangesColourFromOrangeToRedForFailedTest()
	{
		def node = createTreeNodeForIncompleteAsyncTest()
		
		def failureXml = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			failure(message:'baws - the async text failed after some time.')
		}
		
		testee.updateNode(node, failureXml)
		
		assertEquals(ResultTreeNodeColour.RED, node.colour)
		assertEquals(ResultTreeNodeColour.RED, node.getChildAt(0).colour)
	}
	
	@Test
	void testUpdateNode_ChangesTextOfNodeToMatchXml()
	{
		def node = createTreeNodeForIncompleteAsyncTest()
		println "Before [${node.toString()}]"
		assertFalse('new node text'.equals(node.toString()))
		
		def successXml = xmlBuilder.testcase(name:'new node text')
		{
			success(message:'yay - the async text completed after some time.')
		}
		
		testee.updateNode(node, successXml)
		println "After [${node.toString()}]"
		assertTrue('new node text'.equals(node.toString()))
	}
	
	@Test
	void testUpdateNode_ChangesTextOfChildNodeFromIncompleteToSuccessForSuccessfulTest()
	{
		def node = createTreeNodeForIncompleteAsyncTest()
		assertFalse('message: yay - the async text completed after some time.'.equals(node.getChildAt(0).toString()))
		
		def successXml = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			success(message:'yay - the async text completed after some time.')
		}
		
		testee.updateNode(node, successXml)
		assertTrue('message: yay - the async text completed after some time.'.equals(node.getChildAt(0).toString()))
	}
	
	@Test
	void testUpdateNode_HandlesMismatchedTreeNodesAndXml()
	{
		def treeNodeWithNoChildren = new ResultTreeNode(
			'I have no success, failure or incomplete child nodes. I probably should though',
			ResultTreeNodeColour.ORANGE)
		
		def xmlWithSuccessChildElement = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			success(message:'I have no corresponding treenode')
		} 
		
		testee.updateNode(treeNodeWithNoChildren, xmlWithSuccessChildElement)
	}
	
	private def createTreeNodeForIncompleteAsyncTest()
	{
		def xml = xmlBuilder.testcase(name:'assertAsyncNotEquals')
		{
			incomplete(message:'some user message for incomplete asynchronous test')
		}
		
		def runResult = {Object[] args -> return xml} as RunResult
		
		def node = testee.createNode(runResult)
		
		assertEquals(ResultTreeNodeColour.ORANGE, node.colour)
		assertEquals(1, node.childCount)
		assertEquals('message: some user message for incomplete asynchronous test', node.getChildAt(0).toString())
		
		return node
	}
	
	private def getTreeNodeCountWithPattern(rootTreeNode, patternText)
	{
		return getTreeNodesWithPattern(rootTreeNode, patternText).size()
	}
	
	private def getTreeNodesWithPattern(rootTreeNode, patternText)
	{
		def result = []
		def pattern = ~/${patternText}/
		
		def treeNodesGetterClosure
		treeNodesGetterClosure =
		{treeNode ->
			if(pattern.matcher(treeNode.toString()).matches())
			{
				result << treeNode
			}
			
			treeNode.children.each
			{childTreeNode->
				treeNodesGetterClosure(childTreeNode)
			}
		}
		
		treeNodesGetterClosure(rootTreeNode)
		return result
	}
}
