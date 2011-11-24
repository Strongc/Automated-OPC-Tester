package ch.cern.opc.ua.clientlib.session;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.ResponseHeader;

public class SessionChannelTestUtils 
{
	public static SessionChannel createMockChannel(final UnsignedInteger subscriptionId, final StatusCode result) throws ServiceFaultException, ServiceResultException 
	{
		SessionChannel channel = mock(SessionChannel.class);
		
		ResponseHeader header = new ResponseHeader();
		header.setServiceResult(result);
		
		CreateSubscriptionResponse response = new CreateSubscriptionResponse();
		response.setResponseHeader(header);
		response.setSubscriptionId(subscriptionId);

		when(channel.CreateSubscription(any(CreateSubscriptionRequest.class))).thenReturn(response);
		return channel;
	}

}
