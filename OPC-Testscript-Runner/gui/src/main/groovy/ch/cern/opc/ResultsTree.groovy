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

import static java.awt.Color.GREEN
import static java.awt.Color.RED

class ResultsTree implements Observer
{
	protected def tree
	
	private def rootNode
	private def treeModel
	private def factory 
	
	def ResultsTree()
	{
		factory = new TreeNodeFactory()
		
		rootNode = new ResultTreeNode('root node', ResultTreeNodeColour.GREEN)
		treeModel = new DefaultTreeModel(rootNode)
		
		tree = new JTree(treeModel)
		tree.setEditable(true)
		tree.setShowsRootHandles(true)
		tree.setCellRenderer(new RedGreenRenderer())
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
			addNode(testsuiteNode, factory.createNode(newResult))
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
	
	private class RedGreenRenderer extends DefaultTreeCellRenderer
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
			
			def colour = (containsFailures(value)? Color.RED: Color.GREEN)
			backgroundNonSelectionColor = colour
			
			return this
		}
		
		private def containsFailures(ResultTreeNode node)
		{
			def failedChildren = []
			
			def failureCheckerClosure
			failureCheckerClosure = 
			{currentNode->
				if(ResultTreeNodeColour.RED == currentNode.colour)
				{
					failedChildren << currentNode
				}	
				
				currentNode.children.each 
				{childNode ->
					failureCheckerClosure(childNode)
				}
			}
			
			failureCheckerClosure(node)
			
			return failedChildren.size > 0
		}
	} 
}
