== To build all the modules, run command
'mvn clean install'
from this directory

== To build an executable, run command
'mvn assembly:single'
from directory 'gui'
the output of the maven target prints out where the executable jar file goes.

== To run the executable jar file, 
locate the output (see comment above) and double click it.

== Running from eclipse (debugging)
To run this project from eclipse (useful for debugging etc) follow these steps:
1. generate the eclipse projects; from this directory run mvn eclipse:eclipse
2. open eclipse, import all projects under this directory - should fill your workspace with all sub-projects
3. running the project
      - project main is under project 'gui' class 'ch.cern.opc.Main'
      - jna needs to know where to find the OPC client DLL, provide run configuration with argument -Djna.library.path="C:\Workspace\Automated-OPC-Tester\OPCClient\x64\Debug" (or whichever path dll 'AutomatedOpcTester.dll' lives on your setup).