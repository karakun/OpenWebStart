# IcedTea-Web

This module responsibility is to add icedtea-web fat jar to the local m2 repository.
This is necessary because icedtea-web is not available in any public maven repository.

### Building on TeamCity
The TeamCity jobs for icedtea-web and openwebstart are linked together in such a way that the
jar file is placed at the required spot.

### Building on a local machine
In order for the build to find the icedtea-web jar you need to do the following

1. copy the itw-all-depts-XXXX.jar to the icedtea-web directory (besides the pom.xml)
2. rename the itw-all-depts-XXXX.jar to `all-depts.jar`

At the end the file structure should be the following

```
openwebstart
    + deployment/
    + icedtea-web/
    |     + target/
    |     + all-deps.jar
    |     + pom.xml
    |     + README.md
    + installer/
    + ManifestResourceTransformer/
    + openwebstart/

```

