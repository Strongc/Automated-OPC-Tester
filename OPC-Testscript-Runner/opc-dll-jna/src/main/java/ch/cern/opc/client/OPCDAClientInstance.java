package ch.cern.opc.client;

import static ch.cern.opc.common.Log.logInfo;

import java.util.ArrayList;
import java.util.List;

import ch.cern.opc.common.Datatype;
import ch.cern.opc.common.ItemAccessRight;
import ch.cern.opc.common.ItemValue;

/**
 * A singleton that proxies the {@link ch.cern.opc.client.Client Client} class
 * 
 * @author bfarnham
 *
 */
public class OPCDAClientInstance implements OPCDAClientApi
{
	private static OPCDAClientInstance theInstance = null;
	
	private List<String> opcAddressSpace = new ArrayList<String>();

	/**
	 * The instance of the dll as seen by jna
	 */
	private Client client;
	
	public static OPCDAClientApi getInstance()
	{
		if(theInstance == null)
		{
			logInfo("Creating the one and only client instance");
			theInstance = new OPCDAClientInstance();
		}
		
		return theInstance;
	}
	
	private OPCDAClientInstance()
	{
		client = new Client();		
	}

	@Override
	public boolean init(String host, String server)
	{
		logInfo("Initialising instance for host ["+host+"] server ["+server+"]");
		boolean initialised = client.init(host, server);
		
		sleepToAllowOpcServerToBuildAddressSpace(10);

		opcAddressSpace.clear();
		opcAddressSpace.addAll(client.getItemNames());
		logInfo("Retrieved opc server address space, ["+opcAddressSpace.size()+"] items");
		
		return initialised && !opcAddressSpace.isEmpty();
	}

	private void sleepToAllowOpcServerToBuildAddressSpace(final int durationSecs) {
		try 
		{
			logInfo("Sleeping for ["+durationSecs+"] seconds to allow OPC Server to build address space...");
			for(int i=0; i<durationSecs; i++)
			{
				logInfo((i%2==0?"tick":"tock"));
				Thread.sleep(1000);
			}
			logInfo("OK, ready for business");
		} 
		catch (InterruptedException e) 
		{
			throw new RuntimeException("Failed to wait for OPC Server to start up");
		}
	}

	@Override
	public void end() 
	{
		client.end();
		client = null;
		theInstance = null;
		logInfo("Removed native client library instance");
		
		Runtime.getRuntime().gc();
		logInfo("Requested garbage collector to clean unused native client library references");
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
	public boolean destroyGroup(String groupName) 
	{
		return client.destroyGroup(groupName);
	}

	@Override
	public List<String> getItemNames() 
	{
		return opcAddressSpace;
	}

	@Override
	public boolean addItem(String groupName, String itemPath) 
	{
		return client.addItem(groupName, itemPath);
	}

	@Override
	public ItemValue readItemSync(String groupName, String itemPath) 
	{
		return client.readItemSync(groupName, itemPath);
	}

	@Override
	public boolean writeItemSync(String groupName, String itemPath, String value) 
	{
		return client.writeItemSync(groupName, itemPath, value);
	}
	
	@Override
	public boolean writeItemAsync(String groupName, String itemPath, String value) 
	{
		return client.writeItemAsync(groupName, itemPath, value);
	}

	@Override
	public String getLastError() 
	{
		return client.getLastError();
	}

	@Override
	public void registerAsyncUpdate(OPCDAAsyncUpdateCallback callback) 
	{
		logInfo("Registering the update handler to handle asyncchronous updates from the client");
		client.registerAsyncUpdate(callback);
	}

	@Override
	public boolean readItemAsync(String groupName, String itemPath) 
	{
		return client.readItemAsync(groupName, itemPath);
	}

	@Override
	public ItemAccessRight getItemAccessRights(String groupName, String itemPath) 
	{
		return client.getItemAccessRights(groupName, itemPath);
	}

	@Override
	public Datatype getItemDatatype(String groupName, String itemPath) 
	{
		return client.getItemDatatype(groupName, itemPath);
	}
}
