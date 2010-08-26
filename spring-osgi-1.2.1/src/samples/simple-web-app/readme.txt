================================
== Simple WebApplication Demo ==
================================

1. MOTIVATION

As the name implies, this is a simple web application that runs inside OSGi through Spring DM.

The demo contains 3 maven projects:

* logging
which creates an OSGi fragment with a global log4j configuration.

* war
which contains the actual demo war. The bundle is just an archive that
contains one static resource (index.html), some servlets and some JSP. 
Additionally, the project contains a manifest suitable for web development.

* integration-test
which uses Spring-DM testing framework to execute an integration test. The test will check the
existence of the web application by checking the availability of the html page, the servlet 
and JSP.

2. BUILD AND DEPLOYMENT

This directory contains the source files. For building, Maven 2 and JDK 1.4+ are required.
To start the sample, peform the following steps by:

a) Download the needed libraries. 

This step will download Equinox OSGi platform
as well as the sample dependencies. This step is required only once.
From the sample root, type: 

# mvn -P dependencies,equinox clean package

The command will build all the modules and additionally will download the needed libraries.
The equinox platform and the project dependencies should be available under the war/libs/ 
folder.
If you'd like to delete the libraries, make sure you do not delete war/libs/configuration 
folder.

b) Start Equinox platform using the downloaded libraries.

The sample already contains a proper Equinox config under war/lib/configuration folder.
When you start the Equinox platform, the sample configuration will be automatically loaded.
Run the following command to bootstrap OSGi and the sample (including the web container):

# java -jar war/libs/org.eclipse.osgi.jar

Note that you can interact with the Osgi platform. Type help for more information.
A useful command to see what is available is 'ss'.

c) Test the web application.
To connect to the web app, point your browser at http://localhost:8080/simple-web-app/
You should be welcomed with a page that explains the sample content and also displays the 
sources.

d) Close the OSGi platform
To end the OSGi platform and thus the associated container, use the 'close' command in the 
OSGi console.

3. NOTES
a. Logging

To help debugging possible problems as well notifying the user what is going on, the sample
contains a global log4j configuration installed automatically (specified by the logging 
module). If you would like to modify the configuration, update the log4j.properties, build 
the logging module and then update the log4j bundle (update <log4j.bundle.id>). Note that one
some platforms (Windows) the jar locking prevents the file to be updated while the OSGi 
platform is running. In this cases, you need to shutdown the platform first (close) before
building the project.
