package ch.cern.opc.ua.clientlib.addressspace;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.builtintypes.NodeId;

import static ch.cern.opc.common.Log.logError;

public class AddressSpace 
{
	final NodeDescription root;

	public AddressSpace(final NodeDescription root)
	{
		if(root == null) throw new IllegalArgumentException("Null root node passed to AddressSpace ctor");
		this.root = root;
	}

	@Override
	public String toString() 
	{
		return root.toString();
	}
	
	public int getNodeCount()
	{
		return 1 + root.getSubNodeCount();
	}

	public NodeDescription findNodeById(final NodeId nodeId) 
	{
		try
		{
			return root.findNodeById(nodeId);
		}
		catch(IllegalArgumentException e)
		{
			logError("Failed find node ["+nodeId+"], message ["+e.getMessage()+"]");
			return null;
		}
	}
	
	public NodeDescription[] findNodesById(final NodeId... ids)
	{
		if(ArrayUtils.isEmpty(ids)) return new NodeDescription[0];

		NodeDescription[] result = new NodeDescription[ids.length];
		for(int i=0; i<ids.length; i++)
		{
			result[i] = findNodeById(ids[i]);
		}
		return result;
	}
}
