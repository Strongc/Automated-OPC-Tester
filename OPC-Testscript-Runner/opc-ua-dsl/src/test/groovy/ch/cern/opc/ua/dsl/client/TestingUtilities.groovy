package ch.cern.opc.ua.dsl.client

abstract public class TestingUtilities 
{
	static void setSingletonStubInstance(singletonClass, singletonInstance)
	{
		singletonClass.metaClass.'static'.instance = {return singletonInstance}
	}
}