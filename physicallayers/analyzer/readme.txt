-----------------------------------------------

JAR Analyzer -	Kirk Knoernschild
		www.kirkk.com
		www.extensiblejava.com

-----------------------------------------------

IF YOU'VE DOWNLOADED THE SRC VERSION...
-----------------------------------------------
First, unzip the contents of the zip file to any directory you wish.

Next, you'll need to build the project. The src version includes a build.xml file (but no binaries), and requires that you have ANT installed on your machine. Assuming you have ANT installed, and ANT is in your path, you should unzip the Analyzer. In the root directory, simply type ant. After the build runs, you'll find the following directories have been created:

* bin - The binary distribution. If you want to run the Analyzer, navigate to this directory and execute the run.bat file. It'll prompt you for an output file name.
* deploy - This directory contains the binary and source distributions of Analyzer. If you've made changes to Analyzer, you can use the zip files in this directory to deploy the binary and source distributions. Each of the zip files will contain everything needed to run or build Analyzer.

If you have created a new BIN version with modifications, and want to easily explain to others how to use the BIN distribution, read the BIN version instructions below.

-----------------------------------------------

IF YOU'VE DOWNLOADED THE BIN VERSION
-----------------------------------------------
First, unzip the contents of the zip file to any directory you wish.

To run the analyzer, navigate to that directory, and execute the run.bat file. It'll prompt you for an output file.
