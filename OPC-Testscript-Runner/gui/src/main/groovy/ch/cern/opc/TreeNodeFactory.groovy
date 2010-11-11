package ch.cern.opc;

import org.w3c.dom.Element

import com.sun.org.apache.bcel.internal.generic.NEW;

import groovy.xml.dom.DOMCategory
import javax.swing.tree.DefaultMutableTreeNode as TreeNode

public class TreeNodeFactory extends DOMCategory
{
	public static final def TESTCASE_ELM = 'testcase'
	public static final def EXCEPTION_ELM = 'exception'
	public static final def LINE_ELM = 'line'
	
	def createNodes(element)
	{
		use(DOMCategory)
		{
			def root = null
			
			def nodeCreationClosure
			nodeCreationClosure = 
			{parentTreeNode, xmlNode ->
				def newNode
				switch(xmlNode.name())
				{
					case TESTCASE_ELM:
						newNode = createTestcaseNode(xmlNode)
						break;
					case EXCEPTION_ELM:
						newNode = createExceptionNode(xmlNode)
						break;
					default:
						newNode = new ResultTreeNode(xmlNode.name(), ResultTreeNodeColour.GREEN)
						xmlNode.children().each
						{xmlChildNode->
							nodeCreationClosure(newNode, xmlChildNode)
						}
						break;
				}

				if(parentTreeNode == null)
				{
					root = newNode
				}
				else
				{
					parentTreeNode.add(newNode)
				}
			}
			
			nodeCreationClosure(root, element)
//			element.children().each
//			{child->
//				nodeCreationClosure(root, child)
//			}
			
			return root
		}
	}
	
	protected def createTestcaseNode(element)
	{
		use(DOMCategory)
		{
			verifyCorrectElement(TESTCASE_ELM, element)
			
			if(element.children().size() != 1)
			{
				throw new IllegalStateException('testcase elements should always have exactly one child')
			}
			
			if(element.success.size() == 1)
			{
				return new ResultTreeNode(element.'@name', ResultTreeNodeColour.GREEN) 
			}
			else if(element.failure.size() == 1)
			{
				def result = new ResultTreeNode(element.'@name', ResultTreeNodeColour.RED)
				result.add(new ResultTreeNode("message: ${element.failure[0].'@message'}", ResultTreeNodeColour.RED))
				return result
			}
			
			throw new IllegalStateException('testcase elements should always have exactly one child, either success or failure')
		}
	}
	
	protected def createExceptionNode(element)
	{
		use(DOMCategory)
		{
			verifyCorrectElement(EXCEPTION_ELM, element)
			
			def node = new ResultTreeNode(element.name() + ' message: ' + element.'@message', ResultTreeNodeColour.RED)
			element.children().each 
			{child ->
				verifyCorrectElement(LINE_ELM, child)
				node.add(new ResultTreeNode(child.'@line', ResultTreeNodeColour.RED))
			}
			
			return node
		}
	}
	
	private def verifyCorrectElement(expectedElement, element)
	{
		if(!expectedElement.equals(element.name()))
		{
			throw new IllegalArgumentException("method only valid for [${expectedElement}] elements")
		}
	}
}
