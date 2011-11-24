package ch.cern.opc.ua.clientlib.addressspace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opcfoundation.ua.builtintypes.NodeId;

public class NodeDescription 
{
	private final String browseName;
	private final NodeId nodeId;
	
	private final List<Class<?>> dataTypes = new ArrayList<Class<?>>();
	
	private final List<NodeDescription> children = new ArrayList<NodeDescription>();
	
	public NodeDescription(final String browseName, final NodeId nodeId)
	{
		if(StringUtils.isEmpty(browseName)) throw new IllegalArgumentException("Null browse name passed to NodeDescription ctor");
		if(nodeId == null) throw new IllegalArgumentException("Null nodeId name passed to NodeDescription ctor");
		
		this.browseName = browseName;
		this.nodeId = nodeId;
	}
	
	@Override
	public String toString() 
	{
		return recursiveToString("");
	}
	
	private String recursiveToString(final String prefix)
	{
		String folderMarker = StringUtils.isEmpty(prefix)? "" : "|_";
		StringBuffer result = new StringBuffer(prefix + folderMarker + "Name ["+browseName+"] NodeId ["+(nodeId!=null?nodeId.toString():"null")+"]\n");
		
		for(NodeDescription child : children)
		{
			result.append(child.recursiveToString(prefix+"\t"));
		}
		
		return result.toString();
	}

	public void addChildren(NodeDescription... newChildren) 
	{
		if(newChildren != null)
		{
			for(NodeDescription newChild : newChildren)
			{
				if(newChild != null) children.add(newChild);
			}
		}
	}
	
	public NodeDescription[] getChildren()
	{
		return children.toArray(new NodeDescription[0]);
	}
	
	public int getChildCount()
	{
		return children.size();
	}
	
	public String getBrowseName()
	{
		return browseName;
	}

	public NodeId getNodeId() 
	{
		return nodeId;
	}

	public NodeDescription findNodeById(final NodeId id)
	{
		if(matchesNodeId(id)) return this;
		
		for(NodeDescription child : children)
		{
			NodeDescription node = child.findNodeById(id);
			if(node != null) return node;
		}
		
		return null;
	}
	
	public void clearDataTypes()
	{
		dataTypes.clear();
	}
	
	public void addDataTypes(Class<?>...types)
	{
		if(types == null) return;
		
		for(Class<?> type : types)
		{
			if(type != null)
			{
				dataTypes.add(type);
			}
		}
	}

	public Class<?>[] getDataTypes() 
	{
		return dataTypes.toArray(new Class<?>[0]);
	}
	
	private boolean matchesNodeId(final NodeId nodeId)
	{
		return this.nodeId.equals(nodeId);
	}

	public int getSubNodeCount() 
	{
		int result = children.size();
		
		for(NodeDescription child : children)
		{
			result += child.getSubNodeCount();
		}
		
		return result;
	}

}
