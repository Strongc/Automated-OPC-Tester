package ch.cern.opc

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
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
	void testCreateTestcaseNodeForSuccessfulTestcase()
	{
		def testcaseElement = xmlBuilder.testcase(name:'someAssertionType')
		{
			success(message:'well done, you passed')
		}
		
		def node = testee.createTestcaseNode(testcaseElement)
		
		assertEquals('someAssertionType', node.toString())
		assertEquals(ResultTreeNodeColour.GREEN, node.colour)
		
		def childNode = node.getChildAt(0)
		assertEquals('message: well done, you passed', childNode.toString())
	}
	
	@Test
	void testCreateTestcaseNodeForFailedTestcase()
	{
		def testcaseElement = xmlBuilder.testcase(name:'thisAssertionFails')
		{
			failure(message:'fool, you failed')
		}
		
		def node = testee.createTestcaseNode(testcaseElement)

		assertEquals('thisAssertionFails', node.toString())
		assertEquals(ResultTreeNodeColour.RED, node.colour)
		
		def childNode = node.getChildAt(0)
		
		assertEquals('message: fool, you failed', childNode.toString())
		assertEquals(ResultTreeNodeColour.RED, childNode.colour)
	}
	
	@Test(expected = IllegalArgumentException.class)
	void testCreateTestcaseNodeForNonTestCaseElement()
	{
		testee.createTestcaseNode(xmlBuilder.nonTestCase())
	}
	
	@Test(expected = IllegalStateException.class)
	void testCreateTestcaseNodeForElementWithNoChildren()
	{
		testee.createTestcaseNode(xmlBuilder.testcase())
	}
	
	@Test(expected = IllegalStateException.class)
	void testCreateTestcaseNodeForElementWithInvalidChildren()
	{
		testee.createTestcaseNode(xmlBuilder.testcase()
			{
				invalidChild()	
			}
		)
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
		
		def node = testee.createExceptionNode(xml)
		
		assertEquals('exception message: aaargh', node.toString())
		assertEquals(ResultTreeNodeColour.RED, node.colour)
		assertEquals(3, node.childCount)
	}

	@Test(expected = IllegalArgumentException.class)
	void testCreateExceptionNodeWithInvalidArgument()
	{
		testee.createExceptionNode(xmlBuilder.notAnExceptionElement())
	}
	
	@Test(expected = IllegalArgumentException.class)
	void testCreateExceptionNodeWithInvalidChildren()
	{
		testee.createExceptionNode(xmlBuilder.exception(message:'OK')
			{
				anythingExceptLine()
			}
		)
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
	void testRegexp()
	{
		def patternText = '[a-zA-Z\\ ]*end_of_line'
		def pattern = ~/${patternText}/
		
		assertTrue(pattern.matcher('a a a end_of_line').matches())
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
