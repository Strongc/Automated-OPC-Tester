package ch.cern.opc.ua.clientlib.addressspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
	
	private final static String UNCONNECTED_CHILD_NAME = "unconnectedChild";
	private final static NodeId UNCONNECTED_CHILD_ID = new NodeId(99,99);
	private static final NodeDescription UNCONNECTED_CHILD = new NodeDescription(UNCONNECTED_CHILD_NAME, UNCONNECTED_CHILD_ID);
	
	private AddressSpace testee;
	
	@Before
	public void setup()
	{
		buildNodeTree();
		testee = new AddressSpace(ROOT);
	}
	
	@Test
	public void testFindNodeById_ForInvalidNodeIds()
	{
		assertNull(testee.findNodeById(UNCONNECTED_CHILD.getNodeId()));
	}
	
	@Test
	public void testFindNodeById_ForValidNodeIds()
	{
		assertEquals(ROOT, testee.findNodeById(ROOT.getNodeId()));
		assertEquals(FIRST_CHILD, testee.findNodeById(FIRST_CHILD.getNodeId()));
		assertEquals(SECOND_CHILD, testee.findNodeById(SECOND_CHILD.getNodeId()));
		assertEquals(THIRD_CHILD, testee.findNodeById(THIRD_CHILD.getNodeId()));
		assertEquals(FIRST_GRANDCHILD, testee.findNodeById(FIRST_GRANDCHILD.getNodeId()));
		assertEquals(SECOND_GRANDCHILD, testee.findNodeById(SECOND_GRANDCHILD.getNodeId()));
	}
	
	private static void buildNodeTree()
	{
		FIRST_CHILD.addChildren(FIRST_GRANDCHILD, SECOND_GRANDCHILD);
		
		ROOT.addChildren(FIRST_CHILD);
		ROOT.addChildren(SECOND_CHILD);
		ROOT.addChildren(THIRD_CHILD);
	}
}