package ch.cern.opc

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel

class RootNode extends DefaultMutableTreeNode 
{
	private ResultTreeNodeColour colour
	
	def RootNode()
	{
		super(null)
		this.colour = ResultTreeNodeColour.GREEN
	}
	
	public def setTreeModel(DefaultTreeModel treeModel)
	{
		userObject = treeModel
	}
	
	@Override
	String toString()
	{
		return 'root node'
	}
	
	@Override
	synchronized void nodeChanged(node)
	{
		if(userObject == null) return
		
		for(def n=node; n!=null; n=n.parent)
		{
			userObject.nodeChanged(n)
		}
	}
}
