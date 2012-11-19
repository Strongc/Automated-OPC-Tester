package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.ItemValue
import ch.cern.opc.common.Quality
import static ch.cern.opc.common.Quality.State.*
import static org.junit.Assert.*

protected class AsyncUpdateTestUtils 
{
	public static ItemValue createUpdate(value)
	{
		return new ItemValue(value, GOOD, 'some timestamp', 0)
	}
	
	public static ItemValue createUpdate(value, Quality.State quality)
	{
		return new ItemValue(value, quality, 'some timestamp', 0)
	}
	
	public static ItemValue createUpdate(value, Quality.State quality, timestamp, datatype)
	{
		return new ItemValue(value, quality, timestamp, datatype)
	}
	
	private static def assertTestCaseElementPresentAndNameAttributeIsCorrect(xml, expectedTitle, expectedMessage, expectedChildCount)
	{
		assertEquals('testcase root element should be called -testcase-',
			'testcase', xml.name)
		
		assertTrue('testcase root element name attribute should contain the test type',
			xml.'@name'.contains(expectedTitle))
		
		assertTrue('testcase root element name attribute shoudl contain the user message',
			xml.'@name'.contains(expectedMessage))
		
		assertEquals('Should be a single child element detailing the testcase outcome: pass/fail etc',
			expectedChildCount, xml.size())
	}
}
