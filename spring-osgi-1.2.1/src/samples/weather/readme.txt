==================
== Weather Demo ==
==================

1. MOTIVATION

This demo shows more advanced features of Spring-DM and OSGi.
The application creates a very simple weather information services showing
some best practices in designing an application to take advantage of the
modularity offered by OSGi.


The demo contains several maven projects:

* weather-dao
DAO bundle for the weather service. Reads information from an in-memory storage.

* weather-extension
Provides the sample with its own namespace. The extension contains a 'virtual-bundle'
service that allows creating and installing of bundles created on the fly from various
resources. The feature is also wrapped with its own namespace which becomes available
after installing this bundle.

* weather-service
Provides the actual service implementation

* weather-service-test
Simple consumer (acting as a test) for the weather service. Once installed, the bundle
will query the Weather Service which was retrieved and binded using Spring-DM.

* wiring-bundle
Project that does the OSGi assembly and service publishing/consumption as well as bundle
installation through weather-extension virtual bundle feature.


* weather-service-integration-test
Integration test based on Spring-DM testing framework. Installs all the bundles above and
checks whether the weather service has been properly installed or not by interacting with
the service.


2. BUILD AND DEPLOYMENT

This directory contains the source files.
For building, Maven 2 and JDK 1.4+ are required.

This samples uses Apache Felix BND plugin (http://felix.apache.org/site/maven-bundle-plugin-bnd.html)
which automatically generates the OSGi entries inside the MANIFEST.MF file. While the file
can be created and maintained by hand, using the plugin eases the task considerabily.

Most submodules rely on this plugin with two exceptions:

* weather-service-integration-test
this module doesn't produce any artifact, is just an integration test and thus does not have
to be wrapped as an OSGi bundle

* wiring-bundle
due to some problems regarding resource filtering, normal packaging is used. It is expected
that this module will also rely on the bundle plugin, once it allows more advanced features.