package ch.cern.opc.client;

import static ch.cern.opc.common.Log.logDebug;
import static ch.cern.opc.common.Log.logError;

import java.util.ArrayList;
import java.util.List;

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
			logError("Requested refresh rate ["+refreshRateMs+"] was not met - returned refresh rate ["+nativeActualRefreshRate.longValue()+"]");
			return false;
		}
		return true;
	}

	@Override
	public List<String> getItemNames() 
	{
    	final int nElementSz = 300;
    	final int nNumElements = 1000;
    	
    	List<String> result = new ArrayList<String>();
    	
    	boolean gotEmAll = false;
    	for(int offset = 0; !gotEmAll; offset += nNumElements)
    	{
    		String[] s = createBuffer(nElementSz, nNumElements);
    		gotEmAll = DllInterface.INSTANCE.getItemNames(s, nElementSz, nNumElements, offset);
    		System.out.println("gotEmAll ["+gotEmAll+"] offset ["+offset+"]");
    		
        	for(int i=0; i<nNumElements; i++)
        	{
        		if(!s[i].isEmpty())
        		{
        			System.out.println("item ["+i+"] value ["+s[i]+"]");
        			result.add(s[i]);
        		}
        	}
        	
//        	result.addAll(Arrays.asList(s));
    	}
		
		return result;
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
			return translateCppString(buff);
		}
		else
		{
			logError("item value was not read, group ["+groupName+"] item path ["+itemPath+"]");
			return "ERROR - value was not read";
		}
	}

	@Override
	public boolean writeItemSync(String groupName, String itemPath, String value) 
	{
		return DllInterface.INSTANCE.writeItemSync(groupName, itemPath, value);
	}
	
	@Override
	public boolean writeItemAsync(String groupName, String itemPath, String value) 
	{
		return DllInterface.INSTANCE.writeItemAsync(groupName, itemPath, value);
	}

	@Override
	public String getLastError() 
	{
		byte buff[] = new byte[MAX_BUFF_SZ];
		DllInterface.INSTANCE.getLastError(buff, MAX_BUFF_SZ);
		return translateCppString(buff);
	}
	
	private String translateCppString(byte cppBuff[])
	{
		final int stringTerminatorPosition = findCppStringTerminator(cppBuff);
		logDebug("using cpp string from 0 - " + stringTerminatorPosition);
		
		String cppString = new String(cppBuff);
		if(stringTerminatorPosition > 0)
		{
			return cppString.substring(0, stringTerminatorPosition);
		}
		else
		{
			return cppString;
		}
	}
	
	private int findCppStringTerminator(byte cppBuff[])
	{
		for(int i=0; i<cppBuff.length; i++)
		{
			if(cppBuff[i] == 0)
			{
				return i;
			}
		}
		return -1;
	}
	
	private String[] createBuffer(final int nElementSz, final int nNumElements) {
		String result[] = new String[nNumElements];    	
    	
		for(int i=0; i<nNumElements; i++)
    	{    		
    		result[i] = new String(new char[nElementSz]);
    	}
		return result;
	}
}
