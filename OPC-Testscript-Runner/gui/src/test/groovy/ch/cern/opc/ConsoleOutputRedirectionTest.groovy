package ch.cern.opc

import javax.swing.JTextArea

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;


class ConsoleOutputRedirectionTest 
{
	private def textArea
	 
	@Before
	void setup()
	{
		ConsoleOutputRedirection.textArea = new JTextArea()
	}
	
	@Test
	void testConsoleOutputRedirectionRedirectsConsoleOutput()
	{
		println 'woo'
		assertEquals('', ConsoleOutputRedirection.textArea.text)
		
		use(ConsoleOutputRedirection)
		{
			println 'hoo'
			assertEquals('hoo', ConsoleOutputRedirection.textArea.text)
		}
	}
	
	@Test
	void testConsoleOutputRedirectionRedirectsConsoleOutputForMultipleLines()
	{
		use(ConsoleOutputRedirection)
		{
			println 'line1'
			println 'line2'
			println 'line3'
			assertEquals('line1\nline2\nline3', ConsoleOutputRedirection.textArea.text)
		}
	}
	
	@Test
	void testConsoleOutputRedirectionHandlesNullTextArea()
	{
		ConsoleOutputRedirection.textArea = null
		
		use(ConsoleOutputRedirection)
		{
			try
			{
				println('line1')
			}
			catch(NullPointerException e)
			{
				fail()
			}
		}
	}
}
