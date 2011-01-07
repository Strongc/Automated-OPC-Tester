package ch.cern.opc.client;

import static ch.cern.opc.common.Log.*;

public class Main 
{
	private final static String GROUP_NM = "myGroup";
	
    public static void main(String[] args) throws InterruptedException 
    {
    	logWarning("This is a TEST CLIENT only - for trying out the JNA/DLL interface");
    	ClientInstance.getInstance().init("", "Matrikon.OPC.Simulation");
    	
    	ClientApi opcClient = ClientInstance.getInstance();
    	opcClient.init("", "Matrikon.OPC.Simulation");
    	
    	for(int i=0; i<1000; i++)
    	{
    		String groupName = "arseGroup_"+i;
    		opcClient.createGroup(groupName, 100);
    		
    		opcClient.addItem(groupName, "testGroup.myString");
    		opcClient.writeItemAsync(groupName, "testGroup.myString", groupName);
    		
    		opcClient.destroyGroup(groupName);
/*    	
    	ClientInstance.getInstance().registerAsyncUpdate(new AsyncUpdateCallback() 
    	{
			@Override
			public int onUpdate(String itemPath, String value) 
			{
				System.out.println("onUpdate called with item ["+itemPath+"] value ["+value+"]");
				return 1;
			}
		});
    	
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myString");
    	
    	
    	for(int i=0; i<100; i++)
    	{
    		ClientInstance.getInstance().writeItemAsync(GROUP_NM, "testGroup.myString", "loop_"+i);
    		System.out.println("written async");
    		
    		Thread.sleep(500);
    		
    		ClientInstance.getInstance().readItemAsync(GROUP_NM, "testGroup.myString");
    		System.out.println("read async");
    		
    		Thread.sleep(100);
    	}
  */  	
    	}
    	opcClient.end();

    	System.out.println("completed");
    }
    
	private static void readAndDisplayItemValue(String itemName) 
	{
		ClientApi opcClient = ClientInstance.getInstance();
		
		opcClient.addItem(GROUP_NM, itemName);
    	String value = opcClient.readItemSync(GROUP_NM, itemName);
    	logInfo("group ["+GROUP_NM+"] item ["+itemName+"] value ["+value+"]");
	}
}
