package ch.cern.opc.dsl.common.testing.utils

class TestingUtilities 
{
	static void setSingletonStubInstance(singletonClass, singletonInstance)
	{
		singletonClass.metaClass.'static'.getInstance = {return singletonInstance}
	}
}
