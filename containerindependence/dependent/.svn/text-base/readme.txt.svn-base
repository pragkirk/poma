1. Build service
2. Build client
3. Run startfelix.bat or startfelix.sh
4. install the bundles from the felix shell
	--> install file:service/bin/service.jar
	--> install file:client/bin/client.jar
	--> ps
	--> start {service-bundle-id}
	--> start {client-bundle-id}
   Experiment by starting and stopping client and service to get a feel for OSGi.
5. Now change the service message printed in HelloServiceImpl.java and compile. Then do the following while client is running:
	--> stop {service-bundle-id}
	--> update {service-bundle-id}
	--> start {service-bundle-id}
	--> stop {client-bundle-id}
   When stopping the client bundle, you should see a different goodbye message than what you saw in step 4.