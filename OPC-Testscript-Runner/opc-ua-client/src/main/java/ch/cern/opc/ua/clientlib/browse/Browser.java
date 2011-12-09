package ch.cern.opc.ua.clientlib.browse;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.BuiltinsMap;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResult;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.TimestampsToReturn;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;

public class Browser 
{
	private static final double READ_TIMEOUT = 500.0;

	private final SessionChannel channel;

	private static final ReadValueId NAMESPACE_READ_FILTER = 
		new ReadValueId(
				Identifiers.Server_NamespaceArray,
				Attributes.Value,
				null, null);

	public Browser(final SessionChannel channel)
	{
		if(channel == null) throw new IllegalArgumentException("Null session channel passed to Browser ctor");

		this.channel = channel;
	}

	public String[] browseNamespace()
	{
		System.out.println("Retrieving namespace array for server");

		try 
		{
			return readServerNamespace();
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}

		return new String[] {"Failed to read server namespace"};
	}
	
	public NodeDescription browseAddressspace()
	{
		final NodeDescription rootNode = new NodeDescription("Root", Identifiers.RootFolder);
		addChildNodes(rootNode);
		return rootNode;
	}
	
	private NodeDescription[] getNodeChildren(final NodeId node)
	{
		List<NodeDescription> result = new ArrayList<NodeDescription>();
		
		try 
		{
			BrowseResponse response = channel.Browse(null,null,null, createBrowseDescription(node));
			if(response != null)
			{
				BrowseResult[] browseResults = response.getResults();
				if(isNotEmpty(browseResults))
				{
					for(BrowseResult browseResult: browseResults)
					{
						ReferenceDescription[] references = browseResult.getReferences();
						if(isNotEmpty(references))
						{
							for(ReferenceDescription reference : references)
							{
								if(reference != null)
								{
									result.add(new ChildNodeQueryResult(reference).toNodeDescription());
								}
							}
						}
					}
				}
			}
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}
		
		return result.toArray(new NodeDescription[0]);
	}
	
	private void addChildNodes(final NodeDescription node)
	{
		addChildNodes(node, new HashSet<NodeId>());
	}

	private void addChildNodes(final NodeDescription node, final Set<NodeId> visited)
	{
		if(node == null) return;

		// guard for circular references.
		if(visited.contains(node.getNodeId())) 
		{
			return;
		}
		else
		{
			visited.add(node.getNodeId());
		}
		
		System.out.println("adding child nodes to node ["+node.getBrowseName()+"] id ["+node.getNodeId()+"] visited ["+visited.size()+"]");
		node.addChildren(getNodeChildren(node.getNodeId()));
		
		for(NodeDescription childNode : node.getChildren())
		{
			addChildNodes(childNode, visited);
		}
	}
	
	public boolean getNodeDataTypes(final NodeDescription node)
	{
		node.clearDataTypes();
		
		try 
		{
			final ReadResponse response = channel.Read(
					null, 
					READ_TIMEOUT, 
					TimestampsToReturn.Source, 
					new ReadValueId(node.getNodeId(), Attributes.DataType, null, null) 
				);
			
			if(response != null)
			{
				final DataValue[] values = response.getResults();
				if(values != null)
				{
					for(DataValue value : values)
					{
						Variant v = value.getValue();
						
						if(v.getCompositeClass().equals(NodeId.class))
						{
							NodeId builtInTypeId = (NodeId) v.getValue();
							Class<?> dataType = BuiltinsMap.ID_CLASS_MAP.getRight(builtInTypeId);
							node.addDataTypes(dataType);
						}
					}
				}
			}
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
			return false;
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
			return false;
		}		
			 
		System.out.println("Read datatypes for node ["+node.getBrowseName()+"] - ["+ArrayUtils.toString(node.getDataTypes())+"]");
		return isNotEmpty(node.getDataTypes());
	}

	private BrowseDescription createBrowseDescription(final NodeId node) 
	{
		BrowseDescription result = new BrowseDescription();

		result.setNodeId( node );
		result.setBrowseDirection( BrowseDirection.Forward );
		result.setIncludeSubtypes( true );
		result.setNodeClassMask( NodeClass.Object, NodeClass.Variable, NodeClass.Method);
		result.setResultMask( BrowseResultMask.All );
		
		return result;
	}

	private String[] readServerNamespace() throws ServiceFaultException, ServiceResultException 
	{
		ReadResponse response = channel.Read(null, null, TimestampsToReturn.Neither, NAMESPACE_READ_FILTER);
		return readNamespaceResult(response);
	}

	private String[] readNamespaceResult(ReadResponse response) 
	{
		DataValue[] values = response.getResults();

		if(isNotEmpty(values))
		{
			Variant variant = values[0].getValue();
			if(variant != null && variant.isArray())
			{
				return (String[]) variant.getValue();
			}
		}

		return new String[] {"Failed to find namespace in browse results"};
	}
	
	private NodeId translateToNodeId(ExpandedNodeId nodeId)
	{
		if(nodeId == null) return null;
		
		final IdType idType = nodeId.getIdType();
		final int nsIndex = nodeId.getNamespaceIndex();
		final Object value = nodeId.getValue();
		
		switch (idType) 
		{
		case Numeric: 
			return new NodeId(nsIndex, (UnsignedInteger)value);
		case String:
			return new NodeId(nsIndex, (String)value);
		case Guid:
			return new NodeId(nsIndex, (UUID)value);
		case Opaque:
			return new NodeId(nsIndex, (byte[])value);
		default:
			throw new IllegalArgumentException("Failed to translate expanded node id ["+nodeId+"] with type ["+idType+"]");
		}
	}	
	
	private class ChildNodeQueryResult
	{
		final String name;
		final ExpandedNodeId id;
		
		public ChildNodeQueryResult(final ReferenceDescription reference)
		{
			if(reference == null) throw new IllegalArgumentException("ChildNodeQueryResult ctor received null ptr");
			name = reference.getBrowseName().toString();
			id = reference.getNodeId();
		}
		
		public NodeDescription toNodeDescription()
		{
			return new NodeDescription(name, translateToNodeId(id));
		}
	}
}
