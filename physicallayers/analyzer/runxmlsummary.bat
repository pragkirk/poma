echo off
set TEMP_CLASSPATH=%CLASSPATH%
set CLASSPATH=.;./lib/bcel-5.1.jar;./lib/jakarta-regexp-1.3.jar;./lib;./JarAnalyzer-0.9.1.jar
"%JAVA_HOME%/bin/java" com.kirkk.analyzer.textui.XMLUISummary
set CLASSPATH=%TEMP_CLASSPATH%
set TEMP_CLASSPATH=