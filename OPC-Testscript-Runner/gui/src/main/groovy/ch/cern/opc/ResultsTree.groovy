package ch.cern.opc

import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.JPanel

import java.awt.Component
import java.awt.Color
import java.awt.GridLayout

import javax.swing.text.BadLocationException
import javax.swing.tree.DefaultTreeCellRenderer

import org.apache.commons.lang.NotImplementedException
import org.w3c.dom.Element
import groovy.xml.dom.DOMCategory
import ch.cern.opc.common.Log

import static java.awt.Color.GREEN
import static java.awt.Color.RED

@Mixin(Log)
class ResultsTree implements Observer
{
	protected def tree
	
	private def rootNode
	private def treeModel
	private def factory 
	
	def ResultsTree()
	{
		factory = new TreeNodeFactory()
		
		//rootNode = new ResultTreeNode('root node', ResultTreeNodeColour.GREEN)
		rootNode = new RootNode()
		treeModel = new DefaultTreeModel(rootNode)
		rootNode.treeModel = treeModel
		
		tree = new JTree(treeModel)
		tree.setEditable(true)
		tree.setShowsRootHandles(true)
		tree.setCellRenderer(new RedOrangeGreenRenderer())
	}
	
	def initTree()
	{
		addNode(rootNode, factory.createTestsuiteNode())
	}
	
	def clearResults()
	{
		rootNode.removeAllChildren();
		treeModel.reload();
	}
	
	private def addNode(parent, child)
	{		
		if(parent == null)
		{
			logError("Attempt to insert node into tree without a parent - tree should be initialised before adding any nodes")
			return
		}
		
		treeModel.insertNodeInto(child, parent, parent.childCount)
		tree.scrollPathToVisible(new TreePath(child.path))
	}
	
	private def getTestsuiteNode()
	{
		return rootNode.children[0]
	}
	
	@Override
	void update(Observable observable, Object newResult)
	{
		SwingUtilities.invokeLater 
		{
			// create tree node, have it watch for updates to run result (esp. for async run results: updated later)
			def treeNode = factory.createNode(newResult)
			newResult.addObserver(treeNode)
			
			addNode(testsuiteNode, treeNode)
		}
	}
	
	@Override
	String toString()
	{
		def result = ""
		
		def nodeToStringClosure
		nodeToStringClosure = {node, indentCount ->
			def indentString = ""			
			for(int i=0; i<indentCount; i++)
			{
				indentString += "\t"
			}
			if(indentCount > 0) indentString += '|_'
			
			result += (indentString + node.toString() + "\n")
			
			node.children.each
			{
				nodeToStringClosure(it, indentCount + 1)
			}
		}
		
		nodeToStringClosure(rootNode, 0)
		return result
	}
	
	private class RedOrangeGreenRenderer extends DefaultTreeCellRenderer
	{
		@Override
		public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean sel,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) 
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
			backgroundNonSelectionColor = getNodeColour(value)
			return this
		}
		
		private def getNodeColour(node)
		{
			if(containsNodesWithColour(node, ResultTreeNodeColour.RED))
			{
				return Color.RED
			}
			
			if(containsNodesWithColour(node, ResultTreeNodeColour.ORANGE))
			{
				return Color.ORANGE
			}
			
			return Color.GREEN
		}
		
		private def containsNodesWithColour(node, ResultTreeNodeColour targetColour)
		{
			def colouredNodes = []
			
			def colourCheckerClosure
			colourCheckerClosure = 
			{currentNode->
				if(targetColour == currentNode.colour)
				{
					colouredNodes << currentNode
				}	
				
				currentNode.children.each 
				{childNode ->
					colourCheckerClosure(childNode)
				}
			}
			
			colourCheckerClosure(node)
			
			return colouredNodes.size > 0
		}
	} 
}
