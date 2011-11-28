package ch.cern.opc.ua.clientlib.read;

import static ch.cern.opc.common.Log.logError;

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
	
	public DataValue[] readNodeValue(final NodeDescription node)
	{
		if(node == null)
		{
			logError("Cannot read NULL node");
			return new DataValue[]{};
		}
		
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
				return response.getResults();
			}
		} 
		catch (ServiceFaultException e) 
		{
			logError("readNodeValue failed for node ["+node.getNodeId().toString()+"], message ["+e.getMessage()+"]");
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			logError("readNodeValue failed for node ["+node.getNodeId().toString()+"], message ["+e.getMessage()+"]");
			e.printStackTrace();
		}		
			
		return new DataValue[]{};
	}

	private ReadValueId createReadValueId(final NodeDescription node) 
	{
		return new ReadValueId(node.getNodeId(), Attributes.Value, null, null );
	}
}
