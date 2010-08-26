Spring Dynamic Modules Samples
------------------------------

This folder contains various various demo applications and samples for Spring-DM.

Please see each folder for detailed instructions (readme.txt).

As a general rule, each demo provides an integration tests that bootstraps
the OSGi platform, installs the demo and its dependencies and interacts with 
the demo application.

SAMPLES OVERVIEW
----------------

* simple-service
A simple demo for publishing OSGi services using Spring-DM

* simple-web-app
A simple web application running inside OSGi using Spring-DM. The war contains
servlets, static resources and Java Server Faces (JSF)

* weather
A weather service publisher/consumer application that consists of multiple bundles.

* web-console
A Spring-MVC application that interacts with the OSGi platform.

BUILDING AND DEPLOYMENT
-----------------------

All demos require Maven 2.0.7+ and JDK 1.4+.

Each module should be run from its top folder using maven:

# mvn clean install -P <osgi.platform>

where osgi.platform can be one of the following:

equinox - Eclipse Equinox
knopflerfish - Knopflerfish
felix - Apache Felix

Alternatively, one can run only the samples and compile the project by
using the existing Maven profiles. See the Spring-DM root folder readme-building.txt.

Notice that all modules part of the demo must be installed locally as
the integration tests rely on the local maven repository (at the moment)
for installing the demo dependencies, at least for SNAPSHOT repositories.
This requirement might be discarded in the future.

The demos themselves can be deployed individually to an OSGi platform
either by hand or through an automatic tools such as Pax Construct/Runner.

MISSING DEPENDENCIES
--------------------

There have been reports of Maven not properly downloading a module dependency.
If you do experience missing artifacts, not available in the public repository,
please run maven from the project root while connected to the internet.
If that fails, check your local repository and see whether you have a faulty
dependency retrieved (normally an empty/invalid/incomplete folder).

If everything else fails, please use Spring-DM mailing list/forum.