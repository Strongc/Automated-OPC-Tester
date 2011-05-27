package ch.cern.opc;

import org.w3c.dom.Element
import groovy.xml.DOMBuilder

import com.sun.org.apache.bcel.internal.generic.NEW
import java.lang.IllegalArgumentException

import groovy.xml.dom.DOMCategory
import javax.swing.tree.DefaultMutableTreeNode as TreeNode
import ch.cern.opc.common.Log

@Mixin(Log)
public class TreeNodeFactory extends DOMCategory
{
	public static final def TESTCASE_ELM = 'testcase'
	public static final def EXCEPTION_ELM = 'exception'
	public static final def LINE_ELM = 'line'
	
	private def xmlBuilder = DOMBuilder.newInstance()
	
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

			return root
		}
	}
	
	def createTestsuiteNode()
	{
		return new ResultTreeNode('testsuite', ResultTreeNodeColour.GREEN)
	}
	
	def createNode(runResult)
	{
		def xml = runResult.toXml(xmlBuilder)
		def treeNode = null
		
		use(DOMCategory)
		{
			switch(xml.name())
			{
				case TESTCASE_ELM:
					treeNode = createTestcaseNode(xml)
					break;
				case EXCEPTION_ELM:
					treeNode = createExceptionNode(xml)
					break;
				default:
					throw new IllegalArgumentException('failed to create tree node from runResult [${runResult}]')
			}
		}
		
		return treeNode
	}
	
	/**
	 * This factory also does repairs - method changes existing node according to the XML
	 * passed in here.
	 * @param treeNode
	 * @param resultXml
	 * @return
	 */
	def updateNode(treeNode, resultXml)
	{
		setNodeAndChildNodesColour(treeNode, getNodeColourFromXml(resultXml))
		use(DOMCategory)
		{
			resultXml.children().eachWithIndex
			{childElement, i ->
				if(i < treeNode.childCount)
				{
					treeNode.getChildAt(i).userObject = getTestcaseChildNodeText(childElement)
				}
				else
				{
					logError("Mismatched XML and tree node, XML expects a child at index [${i}] under parent [${treeNode}]: XML\n${resultXml}")
				}
			}
		}
	}
	
	private def setNodeAndChildNodesColour(treeNode, colour)
	{
		def setTreeNodeColourClosure
		
		setTreeNodeColourClosure =
		{node ->
			node.colour = colour
			node.children.each
			{childNode ->
				setTreeNodeColourClosure(childNode)
			}
		}
		
		setTreeNodeColourClosure(treeNode)
	}
	
	private def createTestcaseNode(element)
	{
		use(DOMCategory)
		{
			verifyCorrectElement(TESTCASE_ELM, element)
			
			if(element.children().size() != 1)
			{
				throw new IllegalStateException('testcase elements should always have exactly one child')
			}

			def result = new ResultTreeNode(element.'@name', getNodeColourFromXml(element))
			element.children().each
			{childElement ->
				result.add(createTestcaseChildNode(childElement))
			}
			
			return result
		}
	}
	
	private def createTestcaseChildNode(childElement)
	{
		use(DOMCategory)
		{
			switch(childElement.name())
			{
				case 'success':
					return new ResultTreeNode(getTestcaseChildNodeText(childElement), ResultTreeNodeColour.GREEN)
				case 'failure':
					return new ResultTreeNode(getTestcaseChildNodeText(childElement), ResultTreeNodeColour.RED)
				case 'incomplete':
					return new ResultTreeNode(getTestcaseChildNodeText(childElement), ResultTreeNodeColour.ORANGE)
				default:
					throw new IllegalArgumentException('failed to create tree node chind from element [${element}]')
			}
		}
	}
	
	private def getTestcaseChildNodeText(childElement)
	{
		use(DOMCategory)
		{
			switch(childElement.name())
			{
				case 'success':
					return "message: ${childElement.'@message'}"
				case 'failure':
					return "message: ${childElement.'@message'}"
				case 'incomplete':
					return "message: ${childElement.'@message'}"
				default:
					throw new IllegalArgumentException('unrecognised element [${childElement}]')
			}
		}
	}
	
	private def getNodeColourFromXml(element)
	{
		use(DOMCategory)
		{
			if(element.success.size() == 1)
			{
				return ResultTreeNodeColour.GREEN
			}
			else if(element.failure.size() == 1)
			{
				return ResultTreeNodeColour.RED
			}
			else if(element.incomplete.size() == 1)
			{
				return ResultTreeNodeColour.ORANGE
			}
			
			throw new IllegalStateException("unrecognised testcase child element found [${element[0].name}]")
		}
	}
	
	private def createExceptionNode(element)
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
