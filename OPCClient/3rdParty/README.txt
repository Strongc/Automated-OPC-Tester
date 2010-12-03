
Installation instructions for the 3rd party libs for the OPCLibDLL project.
==========================================================================


Overview
========

This project uses the pantheios logging library for logging from the DLL. Pantheios is an open source, free, C++ logging framework that requires a little bit of setup and build in order to use it in your project. Pantheios in turn relies on another open source project called stlsoft - this project consists of header files only and so requires no build - only setup is telling client projects where the header files are located.

As both stlsoft and pantheios are open source latest versions can be downloaded from:
http://sourceforge.net/projects/stlsoft/
http://sourceforge.net/projects/pantheios/

For ease the zipped download of both are included here - you may choose to download a later version.



Setup and Build
===============

Pantheios depends on the stlsoft library.

Setting up stlsoft
1. Unzip the stlsoft repository to somewhere on your drive. For example C:\3rdParty\CPP\stlsoft-1.9.98
2. Set environment variable STLSOFT to point at this directory

Setting up pantheios
This requires a little more effort since this library requires compilation too using the command line compiler of your MS Visual Studio install.
1. Unzip the pantheios repository to somewhere on your drive. For example C:\3rdParty\CPP\pantheios-1.0.1-beta196
2. Set environment variable PANTHEIOS_ROOT to point at this directory
3. Open a cmd prompt
4. Load the environment variables to allow usage of your VC++ compiler (an exe called nmake) from the command line: To do this run the relevant vcvars32.bat file (it sets up the environment variables for nmake) at the command prompt you just opened. This .bat file is located in the VC++ install folder. For example at C:\Program Files\Microsoft Visual Studio 9.0\VC\bin\vcvars32.bat
5. Now you're ready to build pantheios. Locate the pantheios makefile matching your VC++ install. So for VC9 users with the pantheios source unzipped at the location above (C:\3rdParty\CPP\pantheios-1.0.1-beta196) this will be at: C:\3rdParty\CPP\pantheios-1.0.1-beta196\build\vc9. cd to this directory, (it should contain a makefile) and type 'nmake'. This should result in a few minutes of compiling and building. If this doesn't build check your STLSOFT and PANTHEIOS_ROOT variables are correct in the cmd window.


Usage
=====

Pantheios and StlSoft are already set up in the project - this is just for information.

In order to use Pantheios you also have to tell any C++ project where StlSoft is located. As StlSoft is a headers only project all that is required is to tell your project where those headers are. Right click on your project->Properties(dialog)->ConfigurationProperties->C/C++->General->Additional Include Directories. Add "$(STLSOFT)/include"

To use Pantheios you have to tell your C++ project where a) the headers are and b) where the library files are. First the headers: as above tell your project where the Pantheios headers are by right clicking on your project->Properties(dialog)->ConfigurationProperties->C/C++->General->Additional Include Directories. Add "$(PANTHEIOS_ROOT)/include". Next the library files - You can explicitly link in all the Pantheios lib files you require if you want however by default Pantheios uses implicit linking (it loads its own dependencies by inspecting the headers you include). If you want to use implicit linking then all you have to do is tell your C++ project which directory the lib files are in and Pantheios links in what it needs. Tell your C++ project where the Pantheios lib files are located by right clicking on your project->Properties(dialog)->ConfigurationProperties->Linker->Additional Library Directories.




