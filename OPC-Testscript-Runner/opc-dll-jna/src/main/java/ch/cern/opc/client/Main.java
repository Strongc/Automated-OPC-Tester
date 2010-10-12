package ch.cern.opc.client;

public class Main 
{
	private final static String GROUP_NM = "myGroup";
	
    public static void main(String[] args) 
    {
    	System.out.println("This is a TEST CLIENT only - for trying out the JNA/DLL interface");
    	createGroup();
    	
    	System.out.println(ClientInstance.getInstance().getLastError());
    	
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myBigFloat");
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myBool");
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myLongInt");
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myShortInt");
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.mySmallFloat");
    	ClientInstance.getInstance().addItem(GROUP_NM, "testGroup.myString");
    	
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.myString", "pish");
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.myBool", "1");
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.myShortInt", "123");
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.myLongInt", "123456");
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.mySmallFloat", "1.23");
    	ClientInstance.getInstance().writeItemSync(GROUP_NM, "testGroup.myBigFloat", "123.456");
/*    	
    	readAndDisplayItemValue("testGroup.myBigFloat");
    	readAndDisplayItemValue("testGroup.myBool");
    	readAndDisplayItemValue("testGroup.myLongInt");
    	readAndDisplayItemValue("testGroup.myReadOnly");
    	readAndDisplayItemValue("testGroup.myShortInt");
    	readAndDisplayItemValue("testGroup.mySmallFloat");
    	readAndDisplayItemValue("testGroup.myString");
*/    	
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
