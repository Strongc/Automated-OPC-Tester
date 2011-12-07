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
import org.opcfoundation.ua.core.DeleteSubscriptionsRequest;
import org.opcfoundation.ua.core.DeleteSubscriptionsResponse;
import org.opcfoundation.ua.core.ResponseHeader;

public class MockChannelBuilder
{
	private DeleteSubscriptionsResponse deleteSubscriptionsResponse;
	private CreateSubscriptionResponse createSubscriptionResponse;

	public MockChannelBuilder createSubscriptionResponse(final UnsignedInteger subscriptionId, final StatusCode reponseCode)
	{
		ResponseHeader header = new ResponseHeader();
		header.setServiceResult(reponseCode);

		createSubscriptionResponse = new CreateSubscriptionResponse();
		createSubscriptionResponse.setResponseHeader(header);
		createSubscriptionResponse.setSubscriptionId(subscriptionId);

		return this;
	}

	public MockChannelBuilder deleteSubscriptionResponse(final StatusCode reponseCode)
	{
		ResponseHeader header = new ResponseHeader();
		header.setServiceResult(reponseCode);

		deleteSubscriptionsResponse = new DeleteSubscriptionsResponse();
		deleteSubscriptionsResponse.setResponseHeader(header);

		return this;
	}

	public SessionChannel build() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = mock(SessionChannel.class);

		when(channel.CreateSubscription(any(CreateSubscriptionRequest.class))).
		thenReturn(createSubscriptionResponse);

		when(channel.DeleteSubscriptions(any(DeleteSubscriptionsRequest.class))).
		thenReturn(deleteSubscriptionsResponse);

		return channel;
	}
}
