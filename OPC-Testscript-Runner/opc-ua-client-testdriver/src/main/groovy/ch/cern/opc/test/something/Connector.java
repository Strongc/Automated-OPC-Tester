package ch.cern.opc.test.something;

import static ch.cern.opc.test.something.CertificateUtilities.loadFileFromResource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.PropertyConfigurator;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.core.EndpointDescription;

import ch.cern.opc.ua.clientlib.EndpointSummary;
import ch.cern.opc.ua.clientlib.UaClient;
import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;

public class Connector 
{
//	private static String SERVER_URI = "opc.tcp://localhost:4842";
//	private static String SERVER_URI = "opc.tcp://opc-ua-lnx:4880/";
//	private static String SERVER_URI = "opc.tcp://pcen33068:4841";
	private static String SERVER_URI = "opc.tcp://pcen33068:62541/Quickstarts/DataAccessServer";
	
	private static File PUBLIC_CERTIFICATE = loadFileFromResource("/ClientCert.der", "temporaryPublicCertificateFile.der");
	private static File PRIVATE_KEY = loadFileFromResource("/ClientCert.pfx", "temporaryPrivateKeyFile.der");
	private static final String PASSWORD = "Opc.Sample.Ua.Client";
	private static final boolean LOG_SETTINGS_LOADED = loadLog4jProperties();
	
	public static void main(String[] args) throws IllegalStateException, URISyntaxException, InterruptedException 
	{
		System.out.println("Creating client instance, with certificates, logging initialized ["+LOG_SETTINGS_LOADED+"]");
		UaClient.instance().setCertificate(PUBLIC_CERTIFICATE, PRIVATE_KEY, PASSWORD);
		
		EndpointSummary[] endpoints = getEndpoints(new URI(SERVER_URI));
		
		startSession(endpoints[0]);
		
		browseNamespace();
		
		browseAddressspace();

//		readVariableValue("ns=2;i=0");
//		readVariableValue("ns=2;i=1");
//		readVariableValue("ns=2;i=2");
//		readVariableValue("ns=2;i=3");
//		readVariableValue("ns=2;i=4");
//		
//		readVariableType("ns=2;i=0");

		startSubscription("firstSubscription");
//		monitorNodeValues("firstSubscription", "ns=2;i=0");
//		monitorNodeValues("firstSubscription", "ns=4;s=Counter1");
//		monitorNodeValues("firstSubscription", "ns=4;s=Counter2");
/*	
		for(int i=0; i<0; i++)
		{
			writeVariableValue("var11:2", i%2 == 0? "false" : "true");
			writeVariableValue("var12:2", ""+i);
			writeVariableValue("var13:2", ""+i+"."+i);
			
			Thread.sleep(1000);
		}
*/		
		waitForUserInput("Press enter to close the session and exit the client application");
		
		stopSession();
		
		System.out.println("exiting...");
	}

	private static void startSession(EndpointSummary endpoint) 
	{
		System.out.println("Starting client session (with first endpoint)");
		UaClient.instance().startSession(endpoint);
	}
	
	private static void stopSession() 
	{
		System.out.println("Stopping client session");
		UaClient.instance().stopSession();
	}

	private static void waitForUserInput(final String message) 
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println(message);
		scanner.nextLine();
		System.out.println("user input received, continuing...");
	}	

	private static EndpointSummary[] getEndpoints(URI serverUri)
	{
		System.out.println("Discovering server endpoints");
		EndpointSummary[] endpoints = UaClient.instance().getEndpoints(serverUri);
		
		System.out.println("Server endpoints...");
		for(EndpointSummary endpoint: endpoints)
		{
			System.out.print(endpoint);
		}
		
		return endpoints;
	}
	
	private static void browseNamespace()
	{
		System.out.println("Browsing server namespace");
		String[] namespaceEntries = UaClient.instance().browseNamespace();
		
		for(String entry: namespaceEntries)
		{
			System.out.println("\tnamespace entry ["+entry+"]");
		}
	}
	
	private static void browseAddressspace()
	{
		System.out.println("Browsing server nodes recursively");
		AddressSpace addressSpace = UaClient.instance().browseAddressspace();
		
		System.out.println(addressSpace);
	}
	
	private static void readVariableType(final String variableName)
	{
		System.out.println("Attempting to read type of variable ["+variableName+"]");
		final Class<?>[] types = UaClient.instance().readNodeDataTypes(variableName);
		
		System.out.println("variable ["+variableName+"] has types...");
		for(Class<?> type: types)
		{
			System.out.println("\ttype ["+type.getSimpleName()+"]");
		}
	}
	
	private static void readVariableValue(final String variableName) 
	{
		System.out.println("Attempting to read value of variable ["+variableName+"]");
		DataValue[] values = UaClient.instance().readNodeValue(variableName);
		
		System.out.println("variable ["+variableName+"] has values...");
		for(DataValue value : values)
		{
			System.out.println("\tvalue ["+value.getValue().toString()+"]");
		}
	}
	
	private static void writeVariableValue(final String variableName, String value) 
	{
		System.out.println("Attempting to write value ["+value+"] to variable ["+variableName+"]");
		final boolean isWritten = UaClient.instance().writeNodeValueSync(variableName, value);
		
		System.out.println("Attempted write to variable ["+variableName+"] returned ["+isWritten+"]");
		
	}

	private static void startSubscription(final String name) 
	{
		System.out.println("Starting subscription ["+name+"]");
		
		if(!UaClient.instance().startSubscription(name))
		{
			System.err.println("Failed to start subscription ["+name+"]");
		}
		else
		{
			System.out.println("Started subscription ["+name+"]");
		}
	}
	
	private static void monitorNodeValues(final String subscriptionName, final String... nodes)
	{
		System.out.println("Monitoring nodes ["+ArrayUtils.toString(nodes)+"]");
		
		UaClient.instance().monitorNodeValues(subscriptionName, nodes);
	}
	
	private static boolean loadLog4jProperties()
	{
		final URL resource = Connector.class.getResource("/log.properties");
		System.out.println("Loading log4j properties file ["+resource+"]");
		PropertyConfigurator.configure( resource );
		return true;
	}
}
