This project is a wrapper around an OPCClient (in the OPCClientToolkit). The wrapper (OPCClientToolkitWrapper) handles a bit of state and exposes a nice interface onto which the groovy/java modules (in ../OPC-Testscript-Runner) connect and drive the tests.

Note: The C++ portion of the Automated-OPC-Tester has some dependencies, namely:
Pantheios (A C++ logging front end), which depends on
STLSOFT (A C++ library - headers only).

These have to be installed and environment variables set to point to their root directories, for example set environment varibles:
PATHEIOS_ROOT="C:\3rd_Party\CPP\pantheios-1.01-beta212"
STLSOFT="C:\3rd_Party\CPP\stlsoft-1.9.109"



== Building with maven (maven-nar-plugin) UNDER CONSTRUCTION ==
This solution should eventually be buildable with maven.

Currently the OPCClientTookit project is (with effort) buildable with maven-nar (see project's pom.xml for detail). A few pointers here.

1. Install maven that has been doctored to proxy all package requests via CERN nexus (which contains the maven-nar plugin, maven central does not).
	http://cern.ch/maven/maven-current.zip
	
2. Set the maven environment variable (at a cmd prompt) to use this install:
	SET MVN_HOME=<C:\maven-2.2.1>
	SET PATH=%MVN_HOME%\bin;%PATH%
	
3. Start a cmd prompt with nmake/VStudio vars set: Start>All Programs>Microsoft Visual Studio 2008>Visual Studio Tools>Visual Studio 2008 Command Prompt

4. cd to <this dir>/OPCClientToolkit

5. mvn compile (or mvn -X compile for verbose output)

6. mvn package (to build lib)
	
	

