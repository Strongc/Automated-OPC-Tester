package ch.cern.opc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ch.cern.opc.common.Log.*;

import javax.swing.JTextArea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogTest 
{
	private JTextArea textComponent;
	
	@Before
	public void setup()
	{
		textComponent = new JTextArea();
		Log.setTextComponent(textComponent);
	}
	
	@After
	public void teardown()
	{
		Log.setTextComponent(null);
	}
	
	
	@Test
	public void testTextComponentIsUpdated()
	{
		assertTrue(textComponent.getText().isEmpty());
		
		logError("error");
		assertLoggedContent("error\n");
		
		logWarning("warning");
		logInfo("info");
		assertLoggedContent("error\nwarning\ninfo\n");
	}
	
	@Test
	public void testLogLevelTrace()
	{
		logLevel(LogLevel.TRACE.name());
		
		logError("error");
		logWarning("warning");
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		
		assertLoggedContent("error\nwarning\ninfo\ndebug\ntrace\n");
	}
	
	@Test
	public void testLogLevelDebug()
	{
		logLevel(LogLevel.DEBUG.name());
		
		logError("error");
		logWarning("warning");
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		
		assertLoggedContent("error\nwarning\ninfo\ndebug\n");
	}
	
	@Test
	public void testLogLevelInfo()
	{
		logLevel(LogLevel.INFO.name());
		
		logError("error");
		logWarning("warning");
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		
		assertLoggedContent("error\nwarning\ninfo\n");
	}
	
	@Test
	public void testLogLevelWarn()
	{
		logLevel(LogLevel.WARN.name());
		
		logError("error");
		logWarning("warning");
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		
		assertLoggedContent("error\nwarning\n");
	}	
	
	@Test
	public void testLogAtStringVersion()
	{
		logLevel("WaRn");
		
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		assertLoggedContent("");
		
		logLevel("TrAcE");
		
		logInfo("info");
		logDebug("debug");
		logTrace("trace");
		assertLoggedContent("info\ndebug\ntrace\n");
	}
	
	
	private static void assertLoggedContent(final String expected)
	{
		testThreadSnooze(250);
		assertEquals(expected, Log.textArea.getText());
	}

	/**
	 * Make the test thread sleep to give the GUI event pump a chance. I know, I know...
	 * @param snoozeInMs
	 */
	private static void testThreadSnooze(int snoozeInMs)
	{
		try 
		{
			Thread.sleep(snoozeInMs);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}
