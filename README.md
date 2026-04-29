# swittest-l3-template-kt
Swittest L3 template Kotlin application

This app aims to perform multiple tests with Swittest environment. To get your config and credentials please contact Switstack team.  

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