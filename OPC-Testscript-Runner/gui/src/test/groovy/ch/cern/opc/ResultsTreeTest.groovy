package ch.cern.opc

import ch.cern.opc.scriptRunner.results.RunResult
import ch.cern.opc.scriptRunner.results.ObservableRunResult

import static org.junit.Assert.*
import org.junit.Test;
import org.junit.Before;
import groovy.xml.DOMBuilder

class ResultsTreeTest 
{
	private def xmlBuilder = DOMBuilder.newInstance()
	private def testee
	
	@Before
	void setup()
	{
		testee = new ResultsTree()
		testee.initTree()
	}
	
	@Test
	void testUpdateCreatesTreeNodes()
	{
		def countBefore = treeNodeCount
		
		testee.update(null, createRunResult())
		
		// tiny sleep - swing adds nodes in seperate thread. 
		sleep 250
		println testee
		
		assertEquals('expected to see test node and test message child node', countBefore+2, treeNodeCount)
	}
	
	@Test
	void testTreeNodesForRunResultsObserveTheRunResult()
	{
		def runResult = createRunResult()
		assertEquals(0, runResult.countObservers())
		
		testee.update(null, runResult)
		
		// tiny sleep - swing adds nodes in seperate thread. 
		sleep 250
		println testee
		
		assertEquals(1, runResult.countObservers())
	}

	@Test
	void testClearTreeDiscardsEveythingUnderRoot()
	{
		testee.update(null, createRunResult())
		assertTrue(treeNodeCount > 1)
		
		testee.clearResults()
		assertEquals('only root should remain', 1, treeNodeCount)
	}
	
	@Test
	void testInitTreeAddsTestsuiteNode()
	{
		testee.clearResults()
		assertEquals('only root should remain', 1, treeNodeCount)
		
		testee.initTree()
		assertEquals('testsuite should be added under root', 2, treeNodeCount)
	}
	
	private def createRunResult()
	{
		def xml = xmlBuilder.testcase(name:"some nm")
		{
			success(message:"some msg")
		}
		return [toXml: {arg-> println('result.toXml called'); return xml}] as ObservableRunResult
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
		
		nodeCounterClosure(testee.tree.treeModel.root)
		
		return result
	}
	
}
