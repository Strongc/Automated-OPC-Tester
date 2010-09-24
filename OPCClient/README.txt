This project relies on a C++ library containing an OPC client. The library is driven via the DLL interface in the OPCClientToolkitWrapper project (i.e. the lib is the thing that is wrapped by the OPCClientToolkitWrapper).

== Dependencies==
1. The OPCClientToolKit
The OPCClientToolKit is not available in this repo - it lives in sourceforge at:
http://sourceforge.net/projects/opcclient/
Get the source and unzip it to this directory at OPCClientToolKit.

2. Pantheios C++ logging
The OPCClientToolkitWrapper project logs its actions using the pantheios log library.
Download this from 
http://sourceforge.net/projects/pantheios/
(Note this in turn depends on StlSoft - the details for that dependency are doc'd in the pantheios instructions)