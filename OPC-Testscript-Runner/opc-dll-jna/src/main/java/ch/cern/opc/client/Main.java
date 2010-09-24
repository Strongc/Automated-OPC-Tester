package ch.cern.opc.client;

public class Main 
{
	private final static String GROUP_NM = "myGroup";
	
    public static void main(String[] args) 
    {
    	createGroup();
    	
    	readAndDisplayItemValue("testGroup.myBigFloat");
    	readAndDisplayItemValue("testGroup.myBool");
    	readAndDisplayItemValue("testGroup.myLongInt");
    	readAndDisplayItemValue("testGroup.myReadOnly");
    	readAndDisplayItemValue("testGroup.myShortInt");
    	readAndDisplayItemValue("testGroup.mySmallFloat");
    	readAndDisplayItemValue("testGroup.myString");
    }

	private static void createGroup() 
	{
		ClientApi opcClient = ClientInstance.getInstance();
		
    	opcClient.init("", "Matrikon.OPC.Simulation");
    	opcClient.createGroup(GROUP_NM, 1000);
    	opcClient.getItemNames();
	}

	private static void readAndDisplayItemValue(String itemName) 
	{
		ClientApi opcClient = ClientInstance.getInstance();
		
		opcClient.addItem(GROUP_NM, itemName);
    	String value = opcClient.readItemSync(GROUP_NM, itemName);
    	System.out.println("group ["+GROUP_NM+"] item ["+itemName+"] value ["+value+"]");
	}
}
