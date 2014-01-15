package ch.cern.opc.ua.clientlib.writer;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedLong;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.core.WriteValue;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;

public class WriterTest 
{
	private Writer testee;
	
	private NodeDescription nodeDescription;
	private MockSessionChannel mockSessionChannel;
	
	private final static NodeId NODE_ID = NodeId.decode("ns=1;s=testnode");
	

	@Before
	public void setup()
	{
		mockSessionChannel = new MockSessionChannel();
		testee = new Writer(mockSessionChannel);
		
		nodeDescription = new NodeDescription("theBrowseName", NODE_ID);
		
	}
	
	@Test
	public void testWriteNodeValueSync_String() 
	{
		nodeDescription.addDataTypes(String.class);
		
		String values[] = {"the.test.string"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant("the.test.string"), mockSessionChannel);
	}
	

	@Test
	public void testWriteNodeValueSync_Boolean() 
	{
		nodeDescription.addDataTypes(Boolean.class);
		
		String values[] = {"true"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(Boolean.TRUE), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_Byte()
	{
		nodeDescription.addDataTypes(Byte.class);
		
		String values[] = {"127"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Byte("127")), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_UnsignedByte()
	{
		nodeDescription.addDataTypes(UnsignedByte.class);
		
		String values[] = {"255"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new UnsignedByte("255")), mockSessionChannel);
	}	
	
	
	@Test
	public void testWriteNodeValueSync_Integer()
	{
		nodeDescription.addDataTypes(Integer.class);
		
		String values[] = {"123"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Integer(123)), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_Short()
	{
		nodeDescription.addDataTypes(Short.class);
		
		String values[] = {"123"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Short((short)123)), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_UnsignedShort()
	{
		nodeDescription.addDataTypes(UnsignedShort.class);
		
		String values[] = {"123"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new UnsignedShort((short)123)), mockSessionChannel);
	}	
	
	@Test
	public void testWriteNodeValueSync_Long()
	{
		nodeDescription.addDataTypes(Long.class);
		
		String values[] = {"123"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Long((short)123)), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_UnsignedLong()
	{
		nodeDescription.addDataTypes(UnsignedLong.class);
		
		String values[] = {"123"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new UnsignedLong(123)), mockSessionChannel);
	}		
	
	@Test
	public void testWriteNodeValueSync_Float()
	{
		nodeDescription.addDataTypes(Float.class);
		
		String values[] = {"1.23"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Float(1.23)), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_Double()
	{
		nodeDescription.addDataTypes(Double.class);
		
		String values[] = {"1.23"};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(new Double(1.23)), mockSessionChannel);
	}
	
	@Test
	public void testWriteNodeValueSync_DateTime() throws ParseException
	{
		nodeDescription.addDataTypes(DateTime.class);
		String expected = "2014-01-15T17:52:50.290Z"; 
		
		String values[] = {expected};
		testee.writeNodeValueSync(nodeDescription, values);
		
		assertLastWrittenValue(new Variant(DateTime.parseDateTime(expected)), mockSessionChannel);
	}
	
	
	private static void assertLastWrittenValue(final Variant expectedValue, MockSessionChannel mockSessionChannel)
	{
		assertEquals(1, mockSessionChannel.rcvdWriteValues.size());
		
		Variant actualValue = mockSessionChannel.rcvdWriteValues.get(0);
		assertEquals(expectedValue, actualValue);
	}

	private class MockSessionChannel extends SessionChannel
	{

		private List<Variant> rcvdWriteValues = new ArrayList<Variant>();
		
		public MockSessionChannel() 
		{
			super(null, null, null);
		}
		
		@Override
	    public WriteResponse Write(RequestHeader RequestHeader, WriteValue... NodesToWrite) throws ServiceFaultException, ServiceResultException 
	    {
			for(WriteValue writeValue : NodesToWrite)
			{
				rcvdWriteValues.add(writeValue.getValue().getValue());
			}
			
			StatusCode results[] = {StatusCode.GOOD};
	    	return new WriteResponse(null, results, null);
	    }
	}

}
