package ch.cern.opc.dsl.common.client

public interface UpdateHandler 
{
	/**
	* Implementors of this interface are expected to be able to 
	* handle updates arriving asynchronously from the client.
	* 
	* An object implementing this interface will be registered
	* with an instance of the GenericClient object.
	* 
	* @param itemId identifies the item to which the update pertains
	* @param attributeId identifies the attribute of the item which has been updated
	* @param value the value
	*/
   public void onUpdate(itemId, attributeId, value, quality, type, timestamp);
}
