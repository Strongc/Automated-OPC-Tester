package ch.cern.opc.ua.clientlib.read;

import java.util.ArrayList;
import java.util.List;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;

public class Reader 
{
	private final SessionChannel channel;
	
	public Reader(SessionChannel channel)
	{
		if(channel == null) throw new IllegalArgumentException("Null session channel passed to Reader ctor");
		
		this.channel = channel;
	}
	
	public String[] readNodeValue(final NodeDescription node)
	{
		if(node == null)
		{
			System.err.println("Cannot read NULL node");
			return new String[]{"readNodeValue failed - target node was not specified"};
		}
		
		List<String> results = new ArrayList<String>();
		
		try 
		{
			final ReadResponse response = channel.Read(
					null, 
					500.0, 
					TimestampsToReturn.Source, 
					createReadValueId(node) 
				);
			
			if(response != null)
			{
				final DataValue[] values = response.getResults();
				if(values != null)
				{
					for(DataValue value : values)
					{
						results.add(value.toString());
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
			
		return results.toArray(new String[0]);
	}

	private ReadValueId createReadValueId(final NodeDescription node) 
	{
		return new ReadValueId(node.getNodeId(), Attributes.Value, null, null );
	}
}
