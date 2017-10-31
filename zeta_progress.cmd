@echo off
rem ======================================================================
rem  zeta_progress.cmd     Start script for the ZetaGrid progress utility
rem ----------------------------------------------------------------------
rem
rem  This script sets the environment for the ZetaGrid progress utility.
rem
rem  Please note:
rem
rem  - You must set the variable JAVA_HOME below to point at
rem    your Java development kit or runtime directory.
rem
rem  - This utility reads the progress file 'zeta_zeros.tmp'
rem    which only exists when the ZetaGrid client is running.
rem
rem  Prerequisite: Java Runtime Environment 1.2.2 or higher,
rem                e.g. http://java.sun.com/j2se/1.3/download.html
rem
rem ======================================================================

rem ----------------------------------------------------------------------
rem  JAVA_HOME
rem  Must point at your Java Development Kit installation.
rem  Example: SET JAVA_HOME=C:\program files\ibm\java13
rem ----------------------------------------------------------------------

SET JAVA_HOME=

rem ----------------------------------------------------------------------
rem  JAVA_COMMAND
rem  Creating command to call Java VM
rem ----------------------------------------------------------------------

SET JAVA_COMMAND="%JAVA_HOME%\bin\java"

if not "%JAVA_HOME%" == "" goto javaHomeDefined
echo ========
echo WARNING: JAVA_HOME variable is not set
echo ========
echo This variable should point at your Java development kit installation.
SET JAVA_COMMAND=java
:javaHomeDefined


rem ----------------------------------------------------------------------
rem  ZETA_CLASSPATH
rem  Must contain the required libraries, separated by semicolons
rem ----------------------------------------------------------------------

SET ZETA_CLASSPATH=zeta_client.jar

rem ----------------------------------------------------------------------
rem  ZETA_OPTIONS
rem
rem ----------------------------------------------------------------------

SET ZETA_OPTIONS=-Djava.library.path=.


rem ----------------------------------------------------------------------
rem Executing ZetaGrid client...
rem ----------------------------------------------------------------------

start /low "ZetaGrid progress utility" %JAVA_COMMAND% -cp %ZETA_CLASSPATH% %ZETA_OPTIONS% zeta.ShowProgress
goto end

rem ----------------------------------------------------------------------
rem Exit
rem ----------------------------------------------------------------------
:error
echo You must set JAVA_HOME to point at your Java development kit installation
:end