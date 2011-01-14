package ch.cern.opc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		
		Log.logError("error");
		testThreadSnooze(250);		
		assertEquals("error\n", textComponent.getText());
		
		Log.logWarning("warning");
		Log.logInfo("info");
		Log.logDebug("debug");

		testThreadSnooze(250);
		assertEquals("error\nwarning\ninfo\ndebug\n", textComponent.getText());
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
