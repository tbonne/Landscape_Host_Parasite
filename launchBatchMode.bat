@ECHO OFF
TITLE Landscape-host-parasite model

REM fill out the below information so it is true for your installation
set REPAST_VERSION=2.0.1
set REPAST_SIMPHONY=C:/RepastSimphony-2.0/eclipse/plugins
set REPAST_SIMPHONY_ROOT=%REPAST_SIMPHONY%/repast.simphony.runtime_2.0.1
set REPAST_SIMPHONY_LIB=%REPAST_SIMPHONY_ROOT%/lib

REM Define the Core Repast Simphony Directories and JARs
REM You may not need the line below. I had to reset the classpath (as in the next line)
REM to avoid the character length limit on classpaths (I was getting an "input line is too
REM long" error upon running this file).
set CP=
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.batch_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.runtime_%REPAST_VERSION%/lib/*
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.core_%REPAST_VERSION%/lib/*
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.core_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY_ROOT%/bin/
SET CP=%CP%;%REPAST_SIMPHONY_ROOT%/bin-groovy
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.score.runtime_%REPAST_VERSION%/lib/*
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.score.runtime_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.dataLoader_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.data_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.score_%REPAST_VERSION%/bin
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/saf.core.runtime.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/commons-logging-1.0.4.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/groovy-all-1.5.7.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/javassist-3.7.0.GA.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/jpf.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/jpf-boot.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/log4j-1.2.13.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/xpp3_min-1.1.4c.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%/xstream-1.3.jar
SET CP=%CP%;%REPAST_SIMPHONY%/repast.simphony.data_%REPAST_VERSION%/lib/*
SET CP=%CP%;./bin
REM adding the bin to the classpath (above) is important!

REM Change to the Default Repast Simphony Directory
cd C:/Users/tbonne1/Dropbox/code/SpatialMemory/LHP_dispersal/LHP
REM directory change is needed so that local links don't break when running the
REM program -- e.g. when pulling in data from a folder

REM Start the Model. In the batch version you must include the path to
REM the batch file parameters in addition to the XXX.rs folder containing the model.score file.
java -Xss10M -Xmx400M -cp %CP% repast.simphony.batch.BatchMain -params batch/batch2.xml LHP.rs

REM move back up a level, to where you started
cd ..