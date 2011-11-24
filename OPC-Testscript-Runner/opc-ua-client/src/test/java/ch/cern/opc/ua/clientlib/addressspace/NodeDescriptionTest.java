package ch.cern.opc.ua.clientlib.addressspace;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.builtintypes.NodeId;

public class NodeDescriptionTest 
{
	private NodeDescription testee;
	private final static NodeId DUMMY_NODE_ID = new NodeId(1,1);
	
	private NodeDescription firstGrandchild;
	private NodeDescription secondGrandchild;
	private NodeDescription thirdGrandchild;
	private NodeDescription firstChild;
	private NodeDescription secondChild;
	private NodeDescription thirdChild;

	@Before
	public void setup()
	{
		firstGrandchild = new NodeDescription("firstGrandchild", DUMMY_NODE_ID);
		secondGrandchild = new NodeDescription("secondGrandchild", DUMMY_NODE_ID);
		thirdGrandchild = new NodeDescription("thirdGrandchild", DUMMY_NODE_ID);

		firstChild = new NodeDescription("firstChild", DUMMY_NODE_ID);
		secondChild = new NodeDescription("secondChild", DUMMY_NODE_ID);
		thirdChild = new NodeDescription("thirdChild", DUMMY_NODE_ID);

		firstChild.addChildren(firstGrandchild);
		firstChild.addChildren(secondGrandchild);
		thirdChild.addChildren(thirdGrandchild);

		testee = new NodeDescription("parent", DUMMY_NODE_ID);
		testee.addChildren(firstChild);
		testee.addChildren(secondChild);
		testee.addChildren(thirdChild);
	}
	
	@Test
	public void testGetSubNodeCount()
	{
		assertEquals(0, firstGrandchild.getSubNodeCount());
		assertEquals(2, firstChild.getSubNodeCount());
		assertEquals(6, testee.getSubNodeCount());
	}

	@Test
	public void testToString()
	{
		String actual = testee.toString();

		String expected = 
			"Name [parent] NodeId [ns=1;i=1]\n" + 
			"\t|_Name [firstChild] NodeId [ns=1;i=1]\n" +
			"\t\t|_Name [firstGrandchild] NodeId [ns=1;i=1]\n" +
			"\t\t|_Name [secondGrandchild] NodeId [ns=1;i=1]\n" +
			"\t|_Name [secondChild] NodeId [ns=1;i=1]\n" +
			"\t|_Name [thirdChild] NodeId [ns=1;i=1]\n" + 
			"\t\t|_Name [thirdGrandchild] NodeId [ns=1;i=1]\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testAddChild_IgnoresNullChildren()
	{
		assertEquals(3, testee.getChildCount());

		testee.addChildren((NodeDescription)null);

		assertEquals(3, testee.getChildCount());

		testee.addChildren(new NodeDescription[]{null});

		assertEquals(3, testee.getChildCount());
	}

	@Test
	public void testGetNodeId_GetsNodeId()
	{
		NodeId id = new NodeId(123, 321);
		testee = new NodeDescription("test", id);

		assertEquals(id, testee.getNodeId());
	}

	@Test
	public void testAddDataTypes_HandlesNulls()
	{
		testee = new NodeDescription("test", DUMMY_NODE_ID);
		
		testee.clearDataTypes();
		testee.addDataTypes(null, null, null);
		assertEquals(0, testee.getDataTypes().length);
	}

	@Test
	public void testAddDataTypes_SetSingleDataType()
	{
		testee = new NodeDescription("test", DUMMY_NODE_ID);
		
		testee.addDataTypes(Boolean.class);
		Assert.assertArrayEquals(new Class<?>[]{Boolean.class}, testee.getDataTypes());
	}

	@Test
	public void testSetDataTypes_SetMultipleDataTypes()
	{
		testee = new NodeDescription("test", DUMMY_NODE_ID);

		testee.addDataTypes(Boolean.class, Integer.class, Double.class);

		Assert.assertArrayEquals(new Class<?>[]{Boolean.class, Integer.class, Double.class}, testee.getDataTypes());
	}
	
	@Test
	public void testClearDataTypes_DiscardsExistingDataTypes()
	{
		testee = new NodeDescription("test", DUMMY_NODE_ID);
		testee.addDataTypes(Boolean.class, Integer.class, Double.class);
		assertEquals(3, testee.getDataTypes().length);
		
		testee.clearDataTypes();
		assertEquals(0, testee.getDataTypes().length);
	}
	

}
