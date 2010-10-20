package ch.cern.opc

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode as TreeNode
import org.w3c.dom.Element;
import groovy.xml.dom.DOMCategory

class ResultsTree 
{
	def tree
	def root
	
	def ResultsTree()
	{
		root = new TreeNode('root')
		tree = new JTree(root)
	}
	
	def addResults(Element element)
	{
		use(DOMCategory)
		{
			root.add(new TreeNode(element.'@name'))
		}
	}
}
