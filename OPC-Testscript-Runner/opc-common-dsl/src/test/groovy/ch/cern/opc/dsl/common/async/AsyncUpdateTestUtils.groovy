package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.ItemValue;
import static ch.cern.opc.common.Quality.State.*
import static org.junit.Assert.*

protected class AsyncUpdateTestUtils 
{
	public static ItemValue createUpdate(value)
	{
		return new ItemValue(value, GOOD, 'some timestamp', 0)
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
