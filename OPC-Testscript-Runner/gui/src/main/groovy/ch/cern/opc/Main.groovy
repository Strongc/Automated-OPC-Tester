package ch.cern.opc

public class Main 
{
	public static main(args) 
	{
		println(args)
		def scriptFilePath = args.length > 0? args[0]: null
		
		new ScriptRunnerGui(scriptFilePath).show();
	}
}
