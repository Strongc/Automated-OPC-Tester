package ch.cern.opc.da.dsl

abstract class TestingUtilities 
{
	static void setSingletonStubInstance(singletonClass, singletonInstance)
	{
		singletonClass.metaClass.'static'.getInstance = {return singletonInstance}
	}
}
