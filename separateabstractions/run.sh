echo
echo
echo " ----- RUN FROM THE BIN DIRECTORY AFTER COMPILATION ----- "
echo
echo
java -cp ./:./applicant-1.0.jar:./client-1.0.jar:./loan-intrfc-1.0.jar:./loan-impl-1.0.jar:./calc-impl-1.0.jar:../lib/org.springframework.beans-3.0.0.RELEASE.jar:../lib/org.springframework.context-3.0.0.RELEASE.jar:../lib/org.springframework.core-3.0.0.RELEASE.jar:../lib/org.springframework.asm-3.0.0.RELEASE.jar:../lib/org.springframework.aop-3.0.0.RELEASE.jar:../lib/org.springframework.expression-3.0.0.RELEASE.jar:../lib/log4j-1.2.13.jar:../lib/commons-collections.jar:../lib/commons-logging.jar:xml-apis.jar com.extensiblejava.someclient.SomeClient