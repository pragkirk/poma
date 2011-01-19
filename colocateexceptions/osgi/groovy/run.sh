echo
echo
echo " ----- RUN FROM THE BIN DIRECTORY AFTER COMPILATION ----- "
echo
echo $GROOVY_HOME
echo
echo
java -cp ./:./calc-impl-groovy-1.0.jar:./../lib/groovy-1.7.6.jar::./../lib/commons-cli-1.2.jar:./../lib/asm-3.2.jar:./../lib/antlr-2.7.7.jar:./../../bin/loanfacade-1.0.jar:./../../bin/client-1.0.jar:./../../bin/loan-intrfc-1.0.jar:./../../bin/loan-impl-1.0.jar:../../lib/org.springframework.beans-3.0.0.RELEASE.jar:../../lib/org.springframework.context-3.0.0.RELEASE.jar:../../lib/org.springframework.core-3.0.0.RELEASE.jar:../../lib/org.springframework.asm-3.0.0.RELEASE.jar:../../lib/org.springframework.aop-3.0.0.RELEASE.jar:../../lib/org.springframework.expression-3.0.0.RELEASE.jar:../../lib/log4j-1.2.13.jar:../../lib/commons-collections.jar:../../lib/commons-logging.jar com.extensiblejava.client.LoanClient