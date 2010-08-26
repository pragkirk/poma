SPRING DYNAMIC MODULES FOR OSGI(tm) SERVICE PLATFORMS 
-----------------------------------------------------
http://www.springframework.org/osgi

1. Spring DM BUILDING REQUIREMENT

Spring DM 1.x requires at least JDK 1.4 and Maven 2 for building.
Currently, Maven 2.0.9 is used for building the framework.

1. BUILDING Spring DM

Currently, Spring Dynamic Modules uses Maven 2 to handle the building
process. Since Spring DM runs on multiple OSGi platforms and can be
compiled on various JDKs (currently Sun 1.4, 1.5 and 1.6 have been tested),
Maven profiles have been used to allow the selection of the building
environment.

For more info on Maven profiles, please see this page: 
http://maven.apache.org/guides/introduction/introduction-to-profiles.html

1a. Selecting OSGi platform

The following Maven profiles are available for selecting an OSGi platform:

equinox - Equinox 3.2.x
knopflerfish - Knopflerfish 2.0.x/2.1.x/2.2.x
felix - Apache Felix 1.0.x/1.4.x

The OSGi platform should be always specified otherwise the project will not compile.
We recommend that new users try building using Eclipse Equinox platform which is
considered the default platform.

1b. Running the integration tests

By default the project builds only the distributable modules without running any
integration tests. To run them, one should select the 'it' profile.
Note that 1a) applies, so an OSGi platform still has to be specified:

# mvn -P equinox,it clean install

1c. Running the samples

To compile and install the samples, use the 'samples' profile:

# mvn -P equinox,samples clean install

1d. Using JDK 1.5 +

Spring DM requires JDK 1.4+ for its core infrastructure but provides 1.5 specific
source code such as the annotation support. For this cases, the jdk-1.5+ profile
have been created which should be activated by default (and thus include the 
1.5-specific modules into the build).

To check the Java version used by maven, use the followig command:

# mvn -v

Unfortunately, at the moment (Maven 2.0.9), combining several profiles automatically
activates doesn't seem to be supported (hence the need to always specify an OSGi platform
even when using the default). We hope that in the future, the building process will
be improved in this regards so the proper profiles will be applied automatically based
on the existing environment.
