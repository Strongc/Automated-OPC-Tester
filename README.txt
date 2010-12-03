This project is an automated OPC testing toolkit consisting of the following layers


==The groovy/java layers==

Found under directory OPC-Testscript-Runner. These modules are responsible for running automated scripts that are written in the OPC testing DSL and reporting the results. The results are delivered both to the GUI (shows the normal red/green unit testing type tree) and also via XML in the JUnit format (though not compliant to any JUnit Schema/DTD - I could not find one!). The OPC actions defined in the script are managed by the opc-dll-jna layer which drives an OPC client via a JNA interface.

gui (groovy project - it's a gui)
 |
opc-script-runner (groovy project - executes opc test scripts written in the DSL defined by this module)
 |
opc-dll-jna (java project - interfaces to the dll containing the OPC client)

common (java project - not a layer really, just free floating utils common to all layers e.g. logging)


==The C++ layers==
Found under directory OPCClient. These modules (or projects in VC++ speak) are the OPC Client, a wrapper around the OPCClient and a google test test suite testing some of the wrapper.

OPCClientToolKit (this _is_ the OPC client)
 |
OPCClientToolKitWrapper (this is where the opc-dll-jna interface above meets the OPCClient - it wraps the OPCClient code to give it a workable interface from the groovy/java layers above)

OPCClientToolKitWrapperTests (googletest project testing -some- of the OPCClientToolKitWrapper layer)


