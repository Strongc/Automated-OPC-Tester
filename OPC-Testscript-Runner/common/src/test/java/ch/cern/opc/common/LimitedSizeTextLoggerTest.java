package ch.cern.opc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.swing.JTextArea;

import org.junit.Before;
import org.junit.Test;

public class LimitedSizeTextLoggerTest 
{
	private static final int MAX_CHARS = 20;
	
	private JTextArea textArea;
	private LimitedSizeTextLogger testee;
	
	@Before
	public void setup()
	{
		textArea = new JTextArea();
		testee = new LimitedSizeTextLogger(textArea, MAX_CHARS);
	}
	
	@Test
	public void testGetMaxCharsReturnsMaxChars()
	{
		assertEquals(MAX_CHARS, testee.getMaxChars());
	}
	
	@Test
	public void testPublishAddsTextToTextAreaDocument()
	{
		assertEquals(0, textArea.getDocument().getLength());
		
		String msg = new String("1234567890");
		testee.publish(msg);
		snoozeThreadToAllowAsyncTextAreaUpdate();

		assertEquals(msg.length()+1, getDocLength());
	}
	
	@Test
	public void testMultiplePublishInvocationsNeverAllowsDocumentLengthToBeGreaterThanMaxChars()
	{
		String msg = new String("1234567890");
		
		for(int i=0; i<50; i++)
		{
			testee.publish(msg);
		}
		
		snoozeThreadToAllowAsyncTextAreaUpdate();
		assertTrue(getDocLength() <= MAX_CHARS);
	}
	
	private int getDocLength()
	{
		return textArea.getDocument().getLength();
	}
	
	private void snoozeThreadToAllowAsyncTextAreaUpdate()
	{
		try 
		{
			Thread.sleep(10);
		} 
		catch (InterruptedException e) 
		{
			fail("snooze interruption - interrupted whilst snoozing to allow async update of text area");
		}		
	}
}
