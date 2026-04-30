# swittest-l3-template-kt
Swittest L3 template Kotlin application

This application is designed to conduct comprehensive payment transaction testing within the [Swittest environment](https://docs.switstack.io/swittest/).

The process begins by establishing a connection to Swittest. Once authenticated via a custom protocol, Swittest orchestrates the test suite workflow. 

Upon initialization, the application authenticates with [Switcloud](https://docs.switstack.io/switcloud/) through a dedicated client to manage the payment execution. Transactions are then processed using specific parameters retrieved directly from Swittest.

This application supports both physical and virtual cards provided by Swittest. Users can toggle between these two modes by selecting the appropriate build flavor:
* mokastd: For physical card testing.
* mokavepl: For virtual (emulated) card testing.

Please contact Switstack team to get your config and credentials.  

## Configuration

Before building and running the app, add the following parameters to the **local.properties** file located in the project root directory.

Parameters prefixed with **LOCAL_** are mandatory and used by default. You can also specify **RELEASE_** prefixed values to be used specifically for release builds.
```
SWITSTACK_CLIENT_ATTESTATION_SECRET="secret-provided-by-switstack"

LOCAL_POI_ID="id-provided-by-switstack"
LOCAL_SWITTEST_URL="url-provided-by-switstack"

# optional
RELEASE_POI_ID="id-provided-by-switstack" 
RELEASE_SWITTEST_URL="url-provided-by-switstack"
```

Once configured, the app should run and connect to specified swittest server.