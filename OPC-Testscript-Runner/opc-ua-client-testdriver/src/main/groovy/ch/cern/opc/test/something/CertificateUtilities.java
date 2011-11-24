package ch.cern.opc.test.something;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class CertificateUtilities 
{
	private CertificateUtilities()
	{
		throw new IllegalStateException("Static utility API - this class should not be instantiated");
	}
	
	public static File loadFileFromResource(final String resourceURI, String tempFileName)
	{
		System.out.println("Loading file from resource URL ["+resourceURI+"]");
		
		try 
		{
			InputStream stream = Connector.class.getResourceAsStream(resourceURI);
			
			File result = new File(tempFileName);
			result.deleteOnExit();
			
			FileOutputStream fout = new FileOutputStream(result);
			
			byte[]  buffer = new byte[1024];
			for(int len = stream.read(buffer); len != -1; len = stream.read(buffer))
			{
				fout.write(buffer);
			}
			
			System.out.println("Loaded file from resource URL ["+resourceURI+"] to temporay file ["+result.getAbsolutePath()+"] size ["+result.length()+"]");
			return result;
		} 
		catch (IOException e) 
		{
			System.out.println("Failed to read temporary file from resource ["+resourceURI+"], exiting...");
			e.printStackTrace();
		}
		
		return null;
	}

}
