package ch.cern.opc.ua.clientlib.addressspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;

public class AddressSpaceTest 
{
	private static final String ROOT_NAME = "Root";
	private final static NodeDescription ROOT = new NodeDescription(ROOT_NAME, Identifiers.RootFolder);
	
	private static final String FIRST_CHILD_NAME = "firstChild";
	private final static NodeId FIRST_CHILD_ID = new NodeId(1,1);
	private static final NodeDescription FIRST_CHILD = new NodeDescription(FIRST_CHILD_NAME, FIRST_CHILD_ID);
	
	private static final String SECOND_CHILD_NAME = "secondChild";
	private final static NodeId SECOND_CHILD_ID = new NodeId(1,2);
	private static final NodeDescription SECOND_CHILD = new NodeDescription(SECOND_CHILD_NAME, SECOND_CHILD_ID);
	
	private static final String THIRD_CHILD_NAME = "thirdChild";
	private final static NodeId THIRD_CHILD_ID = new NodeId(1,3);
	private static final NodeDescription THIRD_CHILD = new NodeDescription(THIRD_CHILD_NAME, THIRD_CHILD_ID);
	
	private final static String FIRST_GRANDCHILD_NAME = "firstGrandChild";
	private final static NodeId FIRST_GRANDCHILD_ID = new NodeId(2,1);
	private static final NodeDescription FIRST_GRANDCHILD = new NodeDescription(FIRST_GRANDCHILD_NAME, FIRST_GRANDCHILD_ID);
	
	private final static String SECOND_GRANDCHILD_NAME = "secondGrandChild";
	private final static NodeId SECOND_GRANDCHILD_ID = new NodeId(2,2);
	private static final NodeDescription SECOND_GRANDCHILD = new NodeDescription(SECOND_GRANDCHILD_NAME, SECOND_GRANDCHILD_ID);
	
	
	private AddressSpace testee;
	
	@Before
	public void setup()
	{
		buildNodeTree();
		testee = new AddressSpace(ROOT);
	}
	
	@Test
	public void testStringToNodeId_ForInvalidNodeIds()
	{
		assertStringToNodeIdThrowsException(testee, "ns=not a number");
		assertStringToNodeIdThrowsException(testee, "ns=ns=ns=");
		assertStringToNodeIdThrowsException(testee, "i=not a number");
		assertStringToNodeIdThrowsException(testee, "ns=0;y=there is no y format");
		assertStringToNodeIdThrowsException(testee, "i:666");
	}
	
	private static void assertStringToNodeIdThrowsException(AddressSpace testee, final String nodeIdAsString)
	{
		try
		{
			testee.stringToNodeId(nodeIdAsString);
			fail("expected IllegalArgumentException to be thrown");
		}
		catch(IllegalArgumentException e){/*expected*/}
	}
	
	@Test
	public void testFindNodeById_ForValidNodeIds()
	{
		assertEquals(ROOT, testee.findNodeById(ROOT.getNodeId().toString()));
		assertEquals(FIRST_CHILD, testee.findNodeById(FIRST_CHILD.getNodeId().toString()));
		assertEquals(FIRST_GRANDCHILD, testee.findNodeById(FIRST_GRANDCHILD.getNodeId().toString()));
	}
	
	@Test
	public void testFindNodeById_ForInvalidNodeIds()
	{
		assertNull(testee.findNodeById(null));
		assertNull(testee.findNodeById(" "));
		assertNull(testee.findNodeById("invalid node id"));
		assertNull(testee.findNodeById("ns=not a number"));
		assertNull(testee.findNodeById("i=not a number"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStringToNodeId_NullString()
	{
		testee.stringToNodeId(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStringToNodeId_BlankString()
	{
		testee.stringToNodeId("  ");
	}
	
	@Test
	public void testStringToNodeId_DefaultsToNamespaceZero()
	{
		NodeId expected = new NodeId(0, "namespace is zero");
		assertEquals(expected, testee.stringToNodeId("s=namespace is zero"));
		assertEquals(expected, testee.stringToNodeId("ns=0;s=namespace is zero"));
	}
	
	@Test
	public void testStringToNodeId_ForStringIdentifiers()
	{
		NodeId expected = new NodeId(80, "my string identifier");
		assertEquals(expected, testee.stringToNodeId("ns=80;s=my string identifier"));
	}
	
	@Test
	public void testStringToNodeId_ForGUIDIdentifiers()
	{
		final UUID id = UUID.randomUUID();
		NodeId expected = new NodeId(90, id);
		assertEquals(expected, testee.stringToNodeId("ns=90;g="+id.toString()));
	}
	
	@Test
	public void testStringToNodeId_ForOpaqueIdentifiers()
	{
		String id = "abc123";
		NodeId expected = new NodeId(100, id.getBytes());
		assertEquals(expected, testee.stringToNodeId("ns=100;b="+id));
	}
	
	@Test
	public void testStringToNodeId_CanRecreateNodeIdFromString()
	{
		assertEquals(
				Identifiers.RootFolder, 
				testee.stringToNodeId(ROOT.getNodeId().toString()));

		assertEquals(
				FIRST_CHILD_ID, 
				testee.stringToNodeId(FIRST_CHILD.getNodeId().toString()));
		
		assertEquals(
				FIRST_GRANDCHILD_ID, 
				testee.stringToNodeId(FIRST_GRANDCHILD.getNodeId().toString()));
	}
	
	private static void buildNodeTree()
	{
		FIRST_CHILD.addChildren(FIRST_GRANDCHILD, SECOND_GRANDCHILD);
		
		ROOT.addChildren(FIRST_CHILD);
		ROOT.addChildren(SECOND_CHILD);
		ROOT.addChildren(THIRD_CHILD);
	}
}
