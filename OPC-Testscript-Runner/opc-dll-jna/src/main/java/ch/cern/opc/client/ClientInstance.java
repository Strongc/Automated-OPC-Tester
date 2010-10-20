package ch.cern.opc.client;

import static ch.cern.opc.common.Log.*;

/**
 * A singleton that proxies the {@link ch.cern.opc.client.Client Client} class
 * 
 * @author bfarnham
 *
 */
public class ClientInstance implements ClientApi
{
	private static ClientInstance theInstance = null;

	/**
	 * The instance of the dll as seen by jna
	 */
	private Client client;
	
	public static ClientApi getInstance()
	{
		if(theInstance == null)
		{
			logInfo("Creating the one and only client instance");
			theInstance = new ClientInstance();
		}
		
		return theInstance;
	}
	
	private ClientInstance()
	{
		client = new Client();
	}

	@Override
	public boolean init(String host, String server)
	{
		return client.init(host, server);
	}

	@Override
	public boolean destroy() 
	{
		return client.destroy();
	}

	@Override
	public State getState() 
	{
		return client.getState();
	}

	@Override
	public boolean createGroup(String groupName, long refreshRateMs) 
	{
		return client.createGroup(groupName, refreshRateMs);
	}

	@Override
	public boolean getItemNames() 
	{
		return client.getItemNames();
	}

	@Override
	public boolean addItem(String groupName, String itemPath) 
	{
		return client.addItem(groupName, itemPath);
	}

	@Override
	public String readItemSync(String groupName, String itemPath) 
	{
		return client.readItemSync(groupName, itemPath);
	}

	@Override
	public boolean writeItemSync(String groupName, String itemPath, String value) 
	{
		return client.writeItemSync(groupName, itemPath, value);
	}

	@Override
	public String getLastError() 
	{
		return client.getLastError();
	}
}
