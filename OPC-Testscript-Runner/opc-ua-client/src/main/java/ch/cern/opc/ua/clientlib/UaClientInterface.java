package ch.cern.opc.ua.clientlib;

import java.io.File;
import java.net.URI;

import org.opcfoundation.ua.builtintypes.DataValue;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.notification.OPCUAAsyncUpdateCallback;
import org.opcfoundation.ua.builtintypes.NodeId;

public interface UaClientInterface 
{
	public abstract void setCertificate(final File publicKeyFile, final File privateKeyFile, final String password);

	public abstract EndpointSummary[] getEndpoints(final String serverURI) throws IllegalStateException;

	public abstract void startSession(final EndpointSummary endpoint);

	public abstract void stopSession();

	public abstract String[] browseNamespace();

	public abstract AddressSpace browseAddressspace();

	public abstract DataValue[] readNodeValue(final NodeId nodeId);

	public abstract Class<?>[] readNodeDataTypes(final NodeId nodeId);

	public abstract boolean writeNodeValueSync(final NodeId nodeId, String... values);
	
	public abstract boolean writeNodeValueAsync(final NodeId nodeId, String... values);
	
	public abstract boolean startSubscription(final String subscriptionName);
	
	public abstract boolean deleteSubscription(final String subscriptionName);
	
	public abstract boolean hasSubscription(final String subscriptionName);

	public abstract boolean monitorNodeValues(final String subscriptionName, final NodeId... nodeIds);

	public abstract String getLastError();
	
	public abstract void registerAsyncUpdate(OPCUAAsyncUpdateCallback callback);
}