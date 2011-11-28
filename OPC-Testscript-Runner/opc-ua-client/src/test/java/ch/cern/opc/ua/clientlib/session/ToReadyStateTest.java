package ch.cern.opc.ua.clientlib.session;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import static ch.cern.opc.ua.clientlib.session.SessionState.*;


public class ToReadyStateTest 
{
	private final static String[] VALID_NAMESPACE = {"ns1", "ns2", "ns3"};
	
	private NodeDescription rootNode;
	private NodeDescription childNode;
	private ToReadyState testee;
	
	@Before
	public void setup()
	{
		rootNode = new NodeDescription("rootNode", Identifiers.RootFolder);
		childNode = new NodeDescription("child", new NodeId(0, ""));
				
		testee = new ToReadyState();
	}
	
	@Test
	public void testGetNamespaceReturningNullArrayCausesFailure()
	{
		String[] invalidNamespace = null;
		
		Session invalidNamespaceSession = createMockSession(invalidNamespace, createValidAddresSpace());
		
		assertEquals(ERROR, testee.changeState(invalidNamespaceSession));
	}

	@Test
	public void testValidNamespaceAndAddressSpaceReturnsReadyState()
	{
		Session validSession = createMockSession(VALID_NAMESPACE, createValidAddresSpace());
		
		assertEquals(READY, testee.changeState(validSession));
	}
	
	@Test
	public void testGetAddressSpaceReturningRootOnlyNodeCausesFailure()
	{
		AddressSpace invalidAddressSpace = new AddressSpace(rootNode);
		Session rootOnlySession = createMockSession(VALID_NAMESPACE, invalidAddressSpace);
		
		assertEquals(ERROR, testee.changeState(rootOnlySession));
	}
	
	private AddressSpace createValidAddresSpace() 
	{
		rootNode.addChildren(childNode);
		AddressSpace validAddressSpace = new AddressSpace(rootNode);
		return validAddressSpace;
	}
	
	static Session createMockSession(String[] namespace, AddressSpace addressSpace) 
	{
		Session session = mock(Session.class);
		
		when(session.getNamespace()).thenReturn(namespace);
		when(session.getAddressspace()).thenReturn(addressSpace);
		
		return session;
	}
}
