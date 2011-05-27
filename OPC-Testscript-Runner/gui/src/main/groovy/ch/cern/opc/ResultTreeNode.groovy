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

class ResultTreeNode extends DefaultMutableTreeNode implements Observer
{
	private ResultTreeNodeColour colour
	 
	def ResultTreeNode(text, ResultTreeNodeColour colour)
	{
		super(text)
		this.colour = colour
	}
	
	@Override
	void update(Observable observable, Object newResult)
	{
		TreeNodeFactory t = new TreeNodeFactory()
		t.updateNode(this, newResult)
		println("treenode [${this}] updated with value [${newResult}]")
	}
}
