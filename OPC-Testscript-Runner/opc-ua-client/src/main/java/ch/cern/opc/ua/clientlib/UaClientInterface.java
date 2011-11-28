package ch.cern.opc.ua.clientlib;

import java.io.File;
import java.net.URI;

import org.opcfoundation.ua.builtintypes.DataValue;
import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;

public interface UaClientInterface 
{
	public abstract void setCertificate(final File publicKeyFile, final File privateKeyFile, final String password);

	public abstract EndpointSummary[] getEndpoints(URI serverURI) throws IllegalStateException;

	public abstract void startSession(final EndpointSummary endpoint);

	public abstract void stopSession();

	public abstract String[] browseNamespace();

	public abstract AddressSpace browseAddressspace();

	public abstract DataValue[] readNodeValue(final String nodeId);

	public abstract Class<?>[] readNodeDataTypes(final String nodeId);

	public abstract boolean writeNodeValue(final String nodeId, String... values);

	public abstract boolean startSubscription(final String subscriptionName);

	public abstract boolean monitorNodeValues(final String subscriptionName, final String... nodeIds);

	public abstract String getLastError();
}