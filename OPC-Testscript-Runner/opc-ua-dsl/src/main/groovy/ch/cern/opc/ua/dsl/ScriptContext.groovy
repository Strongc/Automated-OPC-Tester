package ch.cern.opc.ua.dsl

import static ch.cern.opc.test.something.CertificateUtilities.loadFileFromResource;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

import ch.cern.opc.common.Log
import static ch.cern.opc.common.Log.*
import ch.cern.opc.ua.clientlib.EndpointSummary;
import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import ch.cern.opc.ua.dsl.client.OPCUAClient
import ch.cern.opc.dsl.common.results.RunResults;
import static ch.cern.opc.ua.dsl.CertificateUtilities.loadFileFromResource

@Mixin([RunResults, Log])
class ScriptContext 
{
	private static File PUBLIC_CERTIFICATE = loadFileFromResource("/ClientCert.der", "temporaryPublicCertificateFile.der");
	private static File PRIVATE_KEY = loadFileFromResource("/ClientCert.pfx", "temporaryPrivateKeyFile.der");
	private static final String PASSWORD = "Opc.Sample.Ua.Client";
	private static final boolean LOG_SETTINGS_LOADED = loadLog4jProperties('/log.properties');

	def nodes = [:]
	
	private static def instance
	
	def static getInstance()
	{
		if(null == instance)
		{
			instance = new ScriptContext()
		}
		return instance
	}
	
	def ScriptContext()
	{
		instance = this
		setClient(new OPCUAClient())
	}


	def init(serverURL)
	{
		logInfo("Creating client instance, with certificates, logging initialized [${LOG_SETTINGS_LOADED}]");
		UaClient.instance().setCertificate(PUBLIC_CERTIFICATE, PRIVATE_KEY, PASSWORD);
		
		def endpoints = getEndpoints(new URI(serverURL));
		
		startSession(endpoints[0]);
	}

	def node(id)
	{
		if(nodes[id] == null)
		{
			nodes[id] = new Node(id)
		}
		
		return nodes[id]
	}
	
	private EndpointSummary[] getEndpoints(URI serverUri)
	{
		logInfo("Discovering server endpoints for server [${serverUri}]");
		def endpoints = UaClient.instance().getEndpoints(serverUri);
		
		logInfo("Discovered [${endpoints.length}] Server endpoints...");
		endpoints.each() {endpoint->logTrace(endpoint.toString())}
		
		return endpoints;
	}
	
	private static def loadLog4jProperties(resourceURI)
	{
		def resource = ScriptContext.class.getResource(resourceURI);
		println("Loading log4j properties file [${resource}] from URI [${resourceURI}]");
		PropertyConfigurator.configure( resource );
		return true;
	}
	
	private def startSession(endpoint)
	{
		logTrace("Starting client session with endpoint [${endpoint}]");
		UaClient.instance().startSession(endpoint);
	}

}
