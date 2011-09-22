package ch.cern.opc

import static org.junit.Assert.*;
import javax.swing.tree.DefaultTreeModel
import org.junit.Test;
import org.junit.Before;

class NodeChangedTestCategory
{
	static void nodeChanged(DefaultTreeModel treeModel, Object changedNode)
	{
		RootNodeTest.changedNodes << changedNode
	}
}

class RootNodeTest
{
	def testee
	def treeModel
	
	static def changedNodes = []
	
	@Before
	void setup()
	{
		testee = new RootNode()
		treeModel = new DefaultTreeModel(null)
		
		changedNodes.clear()
	}
	
	@Test
	void testRootNodeIsCreatedGreen()
	{
		assertEquals(ResultTreeNodeColour.GREEN, testee.colour)
	}
	
	@Test
	void testRootNodeHasRootNodeText()
	{
		assertEquals('root node', testee.toString())
	}
	
	@Test 
	void testNodeChangedHasNoEffectIfCalledOnRootNodeWithNoUserObject()
	{
		testee.treeModel = null;
		
		use(NodeChangedTestCategory)
		{
			testee.nodeChanged(new Object())
		}
		 
		assertEquals(0, changedNodes.size())
	}
	
	@Test
	void testNodeChangedHasNoEffectIfCalledWithNull()
	{
		testee.treeModel = treeModel
		
		use(NodeChangedTestCategory)
		{
			testee.nodeChanged(null)
		}
		
		assertEquals(0, changedNodes.size())
	}
	
	@Test
	void testNodeChangedIsCalledOnRootNodeUserObject()
	{
		testee.treeModel = treeModel
		
		def node = new Object()
		node.metaClass.parent = null
			
		use(NodeChangedTestCategory)
		{
			testee.nodeChanged(node)
		} 
		
		assertEquals(node, changedNodes[0])
	}
	
	@Test
	void testNodeChangedForNodeCallsNodeChangedForAllAncestors()
	{
		testee.treeModel = treeModel
		
		def parent = new Object()
		def node = new Object()
		
		parent.metaClass.parent = null
		node.metaClass.parent = parent

		use(NodeChangedTestCategory)
		{
			testee.nodeChanged(node)
		}

		assertEquals(node, changedNodes[0])
		assertEquals(parent, changedNodes[1])
	}
}
