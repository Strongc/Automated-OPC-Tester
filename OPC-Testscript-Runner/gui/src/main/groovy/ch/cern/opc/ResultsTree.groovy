package ch.cern.opc

import javax.swing.JTree
import java.awt.Component
import java.awt.Color
import javax.swing.tree.DefaultMutableTreeNode as TreeNode
import javax.swing.tree.DefaultTreeCellRenderer

import org.w3c.dom.Element;
import groovy.xml.dom.DOMCategory

import static java.awt.Color.GREEN
import static java.awt.Color.RED

class ResultsTree 
{
	def tree
	def root
	
	static final def ROOT_NAME = 'test results'
	
	def ResultsTree()
	{
		root = new ResultTreeNode(ROOT_NAME, ResultTreeNodeColour.GREEN)
		tree = new JTree(root)
		tree.setCellRenderer(new RedGreenRenderer())
	}
	
	def addResults(Element xml)
	{
		root.add(new TreeNodeFactory().createNodes(xml))
		tree.treeModel.reload()
	}
	
	def clearResults()
	{
		root.removeAllChildren()
		tree.treeModel.reload()
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
		
		nodeToStringClosure(root, 0)
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
