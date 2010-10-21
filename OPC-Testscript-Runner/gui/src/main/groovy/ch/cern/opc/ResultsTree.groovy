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
	
	static final def SUCCESS_NODE_NAME = 'success'
	static final def FAILURE_NODE_NAME = 'failure'
	static final def ROOT_NAME = 'test results'
	
	def ResultsTree()
	{
		root = new TreeNode(ROOT_NAME)
		tree = new JTree(root)
		tree.setCellRenderer(new RedGreenRenderer())
	}
	
	def addResults(Element xml)
	{
		createNodes(root, xml)
		tree.treeModel.reload()
	}
	
	def clearResults()
	{
		root.removeAllChildren()
		tree.treeModel.reload()
	}
	
	private def createNodes(TreeNode parentNode, xml)
	{
		use(DOMCategory)
		{
			def newNode = createNode(xml)
			xml.children().each{
				createNodes(newNode, it)
			}
			parentNode.add(newNode)
		}
	}
	
	private def createNode(element)
	{
		def result
		
		if(SUCCESS_NODE_NAME.equals(element.name()) || FAILURE_NODE_NAME.equals(element.name()))
		{
			result = new TreeNode(element.'@message')
		}
		else
		{
			result = new TreeNode(element.'@name')
		}
		
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
		
		private def containsFailures(TreeNode node)
		{
			def failedChildren = []
			getFailedTestNodes(node, failedChildren)
			
			return failedChildren.size > 0
		}
		
		private def getFailedTestNodes(TreeNode node, failures)
		{
			if(node.toString().contains('failed:'))
			{
				failures << node
			}
			
			node.children.each
			{
				getFailedTestNodes(it, failures)
			}
		}
		
	} 
}
