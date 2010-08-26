=========================
== Simple Service Demo ==
=========================

1. MOTIVATION

As the name implies, this is a simple demo that illustrates publication
and consumption of services inside OSGi through Spring-DM.

The demo contains 2 maven projects:

* simple-service-bundle
which contains the actual demo bundle. The bundle is just an archive that
contains one public interface and a private implementation. 
The bundle contains under META-INF/spring, the Spring configuration for creating
the service but also the configuration file for publishing the service in OSGi.

* simple-service-integration-test
which uses Spring-DM testing framework to execute an integration test. The test
will check the existence of the service published by the demo and will invoke
several operations on it.

2. BUILD AND DEPLOYMENT

This directory contains the source files.
For building, Maven 2 and JDK 1.4+ are required.