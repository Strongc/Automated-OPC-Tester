package ch.cern.opc.ua.clientlib.addressspace;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.IdType;


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

	public NodeDescription findNodeById(final String nodeId) 
	{
		try
		{
			return root.findNodeById(stringToNodeId(nodeId));
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("Failed find node ["+nodeId+"], message ["+e.getMessage()+"]");
			return null;
		}
	}
	
	public NodeDescription[] findNodesById(final String... ids)
	{
		if(ArrayUtils.isEmpty(ids)) return new NodeDescription[0];

		NodeDescription[] result = new NodeDescription[ids.length];
		for(int i=0; i<ids.length; i++)
		{
			result[i] = findNodeById(ids[i]);
		}
		return result;
	}
	
	protected NodeId stringToNodeId(final String identifier)
	{
		System.out.println("Coverting string ["+identifier+"] to node id");
		
		if(StringUtils.isBlank(identifier))
		{
			throw new IllegalArgumentException("blank node identifier string");
		}
		
		int ns = 0;
		IdType type = null;
		Object value = null;
		
		String[] sections = identifier.split(";");
		for(String section : sections)
		{
			if(section.contains("ns="))
			{
				ns = getNamespace(section);
			}
			else if(section.matches("[iIsSgGbB]=.*"))
			{
				type = getType(section);
				value = getIdentifier(type, section);
			}
		}

		if(type != null && value != null)
		{
			return NodeId.get(type, ns, value);
		}
		
		throw new IllegalArgumentException("failed to parse NodeId from string ["+identifier+"]");
	}
	
	private int getNamespace(final String namespaceSection)
	{
		final String[] subSections = namespaceSection.split("=");
		if(subSections.length == 2)
		{
			try
			{
				return Integer.valueOf(subSections[1]);
			}
			catch(NumberFormatException e){}
		}
		
		throw new IllegalArgumentException("failed to parse out namespace index in ["+namespaceSection+"]");
	}
	
	private IdType getType(final String identifierSection)
	{
		final String[] subSections = identifierSection.split("=");
		if(subSections.length == 2)
		{
			final String typeString = subSections[0];
			if("i".equals(typeString)) return IdType.Numeric;
			if("s".equals(typeString)) return IdType.String;
			if("g".equals(typeString)) return IdType.Guid;
			if("b".equals(typeString)) return IdType.Opaque;
		}
		
		throw new IllegalArgumentException("failed to parse out id type in ["+identifierSection+"]");
	}
	
	private Object getIdentifier(final IdType type, final String identifierSection)
	{
		final String[] subSections = identifierSection.split("=");
		if(subSections.length == 2)
		{
			String value = subSections[1];
			if(StringUtils.isNotBlank(value))
			{
				switch(type)
				{
				case Numeric:
					return UnsignedInteger.parseUnsignedInteger(value);
				case String:
					return value;
				case Guid:
					return UUID.fromString(value);
				case Opaque:
					return value.getBytes();
				}
			}
		}
		
		throw new IllegalArgumentException("failed to parse out id type in ["+identifierSection+"]");
	}
}
