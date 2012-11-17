package ch.cern.opc.client;

import static ch.cern.opc.common.Log.logInfo;
import static ch.cern.opc.common.Log.logWarning;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import ch.cern.opc.common.Log;

public class Main 
{
	private final static String GROUP_NM = "myGroup";
	private final static BlockingDeque<UpdateValue> updatesQueue = new LinkedBlockingDeque<UpdateValue>();
	
    public static void main(String[] args) throws InterruptedException 
    {
    	Log.logLevel("debug");
    	
    	logWarning("This is a TEST CLIENT only - for trying out the JNA/DLL interface");
    	OPCDAClientInstance.getInstance().init("", "Matrikon.OPC.Simulation");
    	
    	OPCDAAsyncUpdateCallback callback = new OPCDAAsyncUpdateCallback(updatesQueue);
    	Runnable updatesHandler = new Runnable()
    	{
			@Override
			public void run() 
			{
				System.out.println("Updater thread started, waiting for updates...");
				while(true)
				{
					try 
					{
						UpdateValue update = Main.updatesQueue.takeFirst();
						System.out.println("update recvd: item ["+update.itemPath+"] value ["+update.value+"] quality ["+update.quality+"] type ["+update.type+"] timestamp ["+update.timestamp+"]");
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					} 
				}
			}
    	};
    	
    	new Thread(updatesHandler).start();
		OPCDAClientInstance.getInstance().registerAsyncUpdate(callback);
    	
		OPCDAClientInstance.getInstance().createGroup(GROUP_NM, 50);
    	OPCDAClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myString");
    	OPCDAClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myShortInt");
    	OPCDAClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myLongInt");
    	OPCDAClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myBigFloat");
    	OPCDAClientInstance.getInstance().addItem(GROUP_NM, "testGroup.mySmallFloat");
    	
    	// let main thread sleep - update handler thread deals with updates
    	Thread.sleep(240000);
    	
/*    	
    	for(int i=0; i<100; i++)
    	{
    		OPCDAClientInstance.getInstance().writeItemAsync(GROUP_NM, "testGroup.myString", "loop_"+i);
    		System.out.println("written async");
    		
    		Thread.sleep(500);
    		
    		OPCDAClientInstance.getInstance().readItemAsync(GROUP_NM, "testGroup.myString");
    		System.out.println("read async");
    		
    		Thread.sleep(100);
    	}
*/    	
    	OPCDAClientInstance.getInstance().end();

    	System.out.println("completed");
    }
    
	private static void readAndDisplayItemValue(String itemName) 
	{
		OPCDAClientApi opcClient = OPCDAClientInstance.getInstance();
		
		opcClient.addItem(GROUP_NM, itemName);
    	ItemValue value = opcClient.readItemSync(GROUP_NM, itemName);
    	logInfo("group ["+GROUP_NM+"] item ["+itemName+"] value ["+value.value+"] quality ["+value.quality+"] timestamp ["+value.timestamp+"] datatype ["+value.datatype+"]");
	}
}
