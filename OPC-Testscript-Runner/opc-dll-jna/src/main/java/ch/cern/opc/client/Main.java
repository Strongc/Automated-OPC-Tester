package ch.cern.opc.client;

import static ch.cern.opc.common.Log.*;

public class Main 
{
	private final static String GROUP_NM = "myGroup";
	
    public static void main(String[] args) 
    {
    	logWarning("This is a TEST CLIENT only - for trying out the JNA/DLL interface");
    	ClientInstance.getInstance().init("", "Matrikon.OPC.Simulation");
    }

	private static void createGroup() 
	{
		ClientApi opcClient = ClientInstance.getInstance();
		
    	opcClient.init("", "Matrikon.OPC.Simulation");
    	opcClient.createGroup(GROUP_NM, 1000);
//    	opcClient.getItemNames();
	}

	private static void readAndDisplayItemValue(String itemName) 
	{
		ClientApi opcClient = ClientInstance.getInstance();
		
		opcClient.addItem(GROUP_NM, itemName);
    	String value = opcClient.readItemSync(GROUP_NM, itemName);
    	logInfo("group ["+GROUP_NM+"] item ["+itemName+"] value ["+value+"]");
	}
}
