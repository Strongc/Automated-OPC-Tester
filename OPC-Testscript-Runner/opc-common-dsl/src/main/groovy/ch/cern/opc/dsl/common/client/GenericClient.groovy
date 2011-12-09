package ch.cern.opc.dsl.common.client

/**
 * Interface represents a generic client to common DSL code. It is expected that
 * OPCDA and OPCUA specific clients will supply an implementation of the interface.
 * These implementations are then used from common DSL code for standard 
 * (i.e. protocol agnostic) interactions with the underlying client.
 * @author bfarnham
 *
 */
public interface GenericClient 
{
	/**
	 * Queries the underlying client layer (OPC-DA/UA)
	 * to gather information on the previous error.
	 */
	public String getLastError()
	
	/**
	 * Pass an implementation of the UpdateHandler interface to the client,
	 * the client will invoke the UpdateHandler on the (asynchronous)
	 * arrival of information from the client (OPC-UA/DA) layer.
	 * @param handler
	 */
	public void registerForAsyncUpdates(UpdateHandler handler)
	
	/**
	 * Deletes all client/server sessions etc. Cleaner as in
	 * Leon the Professional.
	 */
	public void cleanUp();
}
