package ch.cern.opc.ua.clientlib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

import ch.cern.opc.ua.clientlib.session.Session;

public class UaClientTest 
{
	private static final String SUBSCRIPTION_NAME = "name of subscription";
	private static File PUBLIC_CERTIFICATE;
	private static File PRIVATE_KEY;
	private static final String password = "Opc.Sample.Ua.Client";
	
	{
		try 
		{
			PUBLIC_CERTIFICATE = new File(UaClientTest.class.getResource("/ClientCert.der").toURI());
			PRIVATE_KEY = new File(UaClientTest.class.getResource("/ClientCert.pfx").toURI());
		} 
		catch (URISyntaxException e) 
		{
			fail("Test setup failure - failed to load public and private key resources from classpath");
			e.printStackTrace();
		}
	}
	
	private UaClient testee;
	
	@Before
	public void setup()
	{
		testee = (UaClient) UaClient.instance();
	}
	
	@Test
	public void testInstanceExists()
	{
		assertNotNull(testee);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetCertificate_NullCertificateFileThrowsException()
	{
		testee.setCertificate(null, PRIVATE_KEY, password);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetCertificate_NullPrivateKeyThrowsException()
	{
		testee.setCertificate(PUBLIC_CERTIFICATE, null, password);
	}
	
	@Test
	public void testSetCertificate_ValidCertificatesDoesNotThrowException()
	{
		testee.setCertificate(PUBLIC_CERTIFICATE, PRIVATE_KEY, password);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testGetEndpoints_NonExistentServer() throws URISyntaxException
	{
		testee.setCertificate(PUBLIC_CERTIFICATE, PRIVATE_KEY, password);
		
		assertNotNull(testee.getEndpoints(new URI("opc.tcp://I_do_no_exist:69/")));
	}
	
	@Test
	public void testGetLastError()
	{
		assertTrue(StringUtils.isNotEmpty(testee.getLastError()));
	}
	
	@Test
	public void testDeleteSubscriptionSuccess() throws ServiceFaultException, ServiceResultException
	{
		Session mockSession = mock(Session.class);
		when(mockSession.deleteSubscription(any(String.class))).thenReturn(true);
		
		testee.injectTestSession(mockSession);
		
		assertTrue(testee.deleteSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testDeleteSubscriptionFailure() throws ServiceFaultException, ServiceResultException
	{
		Session mockSession = mock(Session.class);
		when(mockSession.deleteSubscription(any(String.class))).thenReturn(false);
		
		testee.injectTestSession(mockSession);
		
		assertFalse(testee.deleteSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRegisterSubscriptionNotificationHandler()
	{
		testee.registerAsyncUpdate(null);
	}
}
