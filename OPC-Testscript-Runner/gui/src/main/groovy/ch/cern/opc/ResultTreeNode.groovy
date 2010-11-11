package ch.cern.opc

import javax.swing.tree.DefaultMutableTreeNode
import groovy.xml.dom.DOMCategory
import java.awt.Color

enum ResultTreeNodeColour 
{
	GREEN(Color.GREEN),
	ORANGE(Color.ORANGE),
	RED(Color.RED)
	
	private final Color colour;
	
	ResultTreeNodeColour(colour)
	{
		this.colour = colour
	}
	
	public def getColour()
	{
		return colour
	}
}

class ResultTreeNode extends DefaultMutableTreeNode 
{
	private final ResultTreeNodeColour colour
	 
	def ResultTreeNode(text, ResultTreeNodeColour colour)
	{
		super(text)
		this.colour = colour
	}
}
