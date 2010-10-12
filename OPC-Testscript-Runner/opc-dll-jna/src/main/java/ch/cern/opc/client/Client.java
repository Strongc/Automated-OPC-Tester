package ch.cern.opc.client;

import com.sun.jna.NativeLong;

class Client implements ClientApi 
{

	private final static int MAX_BUFF_SZ = 1000;
	
	@Override
	public boolean init(String host, String server) 
	{
		DllInterface.INSTANCE.init(host, server);
		return true;
	}

	@Override
	public boolean destroy() 
	{
		return true;
	}

	@Override
	public State getState() 
	{
		return null;
	}

	@Override
	public boolean createGroup(String groupName, long refreshRateMs) 
	{
		
		NativeLong nativeRequestedRefreshRate = new NativeLong(refreshRateMs);
		NativeLong nativeActualRefreshRate = DllInterface.INSTANCE.createGroup(groupName, nativeRequestedRefreshRate);

		if(!nativeActualRefreshRate.equals(nativeRequestedRefreshRate))
		{
			System.err.println("Requested refresh rate ["+refreshRateMs+"] was not met - returned refresh rate ["+nativeActualRefreshRate.longValue()+"]");
			return false;
		}
		return true;
	}

	@Override
	public boolean getItemNames() 
	{
		DllInterface.INSTANCE.getItemNames();
		return true;
	}

	@Override
	public boolean addItem(String groupName, String itemPath) 
	{
		return DllInterface.INSTANCE.addItem(groupName, itemPath);
	}

	@Override
	public String readItemSync(String groupName, String itemPath) 
	{
		byte buff[] = new byte[MAX_BUFF_SZ];
		if(DllInterface.INSTANCE.readItemSync(groupName, itemPath, buff, MAX_BUFF_SZ))
		{
			return new String(buff);
		}
		else
		{
			return "ERROR - value was not read";
		}
	}

	@Override
	public boolean writeItemSync(String groupName, String itemPath, String value) 
	{
		return DllInterface.INSTANCE.writeItemSync(groupName, itemPath, value);
	}

	@Override
	public String getLastError() 
	{
		byte buff[] = new byte[MAX_BUFF_SZ];
		DllInterface.INSTANCE.getLastError(buff, MAX_BUFF_SZ);
		
		return new String(buff);
	}

}
