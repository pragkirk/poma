echo
echo
echo " ----- RUN FROM THE BIN DIRECTORY AFTER COMPILATION ----- "
echo
echo $SCALA_HOME
echo
echo
java -cp ./:$SCALA_HOME/lib/scala-library.jar:./../../bin/loanfacade-1.0.jar:./../../bin/client-1.0.jar:./../../bin/loan-intrfc-1.0.jar:./../../bin/loan-impl-1.0.jar:./calc-impl-scala-1.0.jar:../../lib/org.springframework.beans-3.0.0.RELEASE.jar:../../lib/org.springframework.context-3.0.0.RELEASE.jar:../../lib/org.springframework.core-3.0.0.RELEASE.jar:../../lib/org.springframework.asm-3.0.0.RELEASE.jar:../../lib/org.springframework.aop-3.0.0.RELEASE.jar:../../lib/org.springframework.expression-3.0.0.RELEASE.jar:../../lib/log4j-1.2.13.jar:../../lib/commons-collections.jar:../../lib/commons-logging.jar:xml-apis.jar com.extensiblejava.client.LoanClient