package ch.cern.opc.ua.clientlib.writer;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.Enumeration;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Structure;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedLong;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.core.WriteValue;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import ch.cern.opc.ua.clientlib.browse.Browser;

public class Writer 
{
	private final SessionChannel channel;

	public Writer(final SessionChannel channel)
	{
		if(channel == null) throw new IllegalArgumentException("Null session channel passed to Writer ctor");

		this.channel = channel;
	}

	public boolean writeNodeValue(final NodeDescription node, String... values)
	{
		if(node == null)
		{
			System.err.println("Cannot write  - null node specified");
			return false;
		}
		
		if(isEmpty(values)) 
		{
			System.err.println("Cannot write to node ["+node.getBrowseName()+"], no values specified");
			return false;
		}

		if(isEmpty(node.getDataTypes())) 
		{
			if(!getNodeDataTypes(node))
			{
				System.err.println("Cannot write to node ["+node.getBrowseName()+"] with values ["+ArrayUtils.toString(values)+"], failed to read variable types");
				return false;
			}
		}

		DataValue[] dataValues = translateToDataValues(values, node.getDataTypes());
		WriteValue[] writeValues = createWriteValues(node, dataValues );
		return writeToServer(writeValues);
	}
	
	private boolean getNodeDataTypes(final NodeDescription node)
	{
		return new Browser(channel).getNodeDataTypes(node);
	}

	private boolean writeToServer(WriteValue[] writeValues) {
		try 
		{
			WriteResponse response = channel.Write(null, writeValues);

			for(StatusCode result : response.getResults())
			{
				if(result.isNotGood()) return false;
			}
			return true;
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}

		return false;
	}

	private WriteValue[] createWriteValues(final NodeDescription node, final DataValue[] dataValues) 
	{
		WriteValue[] result = new WriteValue[dataValues.length];
		for(int i=0; i<dataValues.length; i++)
		{
			final WriteValue writeValue = new WriteValue();

			writeValue.setNodeId(node.getNodeId());
			writeValue.setValue(dataValues[i]);
			writeValue.setAttributeId(Attributes.Value);

			if(WriteValue.validate(writeValue) != null)
			{
				throw new IllegalArgumentException("WriteValue ["+writeValue+"] is invalid");
			}
			result[i] = writeValue;
		}

		return result;
	}

	private DataValue[] translateToDataValues(final String[] values, final Class<?>[] targetDataTypes)
	{
		if(values.length != targetDataTypes.length) throw new IllegalArgumentException("Cannot translate values [length: "+values.length+"] to target data types [length: "+targetDataTypes.length+"] - different array lengths");

		DataValue[] result = new DataValue[values.length];

		for(int i=0; i<values.length; i++)
		{
			final Variant variant = translateValueToVariant(
					values[i], 
					(Class<?>)targetDataTypes[i]);

			final DataValue dataValue = new DataValue(variant);
			result[i] = dataValue;
		}

		return result;
	}

	private Variant translateValueToVariant(final String value, final Class<?> clazz) 
	{
		System.out.println("Converting value ["+value+"] to type ["+clazz.getSimpleName()+"]");

		final Object valueAsObject = convertStringToType(value, clazz);
		if(valueAsObject == null)
		{
			throw new IllegalArgumentException("Failed to convert string value ["+value+"] to class type ["+clazz.getSimpleName()+"]");
		}
		else
		{
			System.out.println("Converted string value ["+value+"] to object ["+valueAsObject.toString()+"]");
		}

		return new Variant(valueAsObject);
	}

	private Object convertStringToType(final String value, final Class<?> clazz)
	{
		if (clazz.equals(byte[].class))
			return null;
		if (clazz.equals(Boolean.class))
			return Boolean.valueOf(value);
		if (clazz.equals(Byte.class))
			return null;
		if (clazz.equals(UnsignedByte.class) && !clazz.isArray())
			return null;
		if (clazz.equals(Short.class))
			return null;
		if (clazz.equals(UnsignedShort.class))
			return null;
		if (clazz.equals(Integer.class))
			return Integer.valueOf(value);
		if (clazz.equals(UnsignedInteger.class))
			return null;
		if (clazz.equals(Long.class))
			return null;
		if (clazz.equals(UnsignedLong.class))
			return null;
		if (clazz.equals(Float.class))
			return null;
		if (clazz.equals(Double.class))
			return Double.valueOf(value);
		if (clazz.equals(String.class))
			return value;
		if (clazz.equals(DateTime.class))
			return null;
		if (clazz.equals(UUID.class))
			return null;
		if (clazz.equals(XmlElement.class))
			return null;
		if (clazz.equals(NodeId.class))
			return null;
		if (clazz.equals(ExpandedNodeId.class))
			return null;
		if (clazz.equals(StatusCode.class))
			return null;
		if (clazz.equals(QualifiedName.class))
			return null;
		if (clazz.equals(LocalizedText.class))
			return null;
		if (clazz.equals(Structure.class))
			return null;
		if (clazz.equals(ExtensionObject.class))
			return null;
		if (clazz.equals(DataValue.class))
			return null;
		if (clazz.equals(DiagnosticInfo.class))
			return null;
		if (clazz.equals(Variant.class))
			return null;
		if (Structure.class.isAssignableFrom(clazz))
			return null;
		if (Enumeration.class.isAssignableFrom(clazz))
			return null;
		throw new IllegalArgumentException("Variant cannot be "
				+ clazz.getCanonicalName());		
	}

}
