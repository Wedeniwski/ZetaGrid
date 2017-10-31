@echo off
rem ======================================================================
rem  zeta.bat        Start script for the ZetaGrid client
rem ----------------------------------------------------------------------
rem
rem  This script sets the environment for the ZetaGrid client.
rem
rem  Please note:
rem
rem  - You must set the variable JAVA_HOME below to point at
rem    your Java development kit or runtime directory.
rem
rem  - If you have to access the Internet through a proxy server
rem    you must set the variable PROXY_HOST below to your proxy's address
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
rem  PROXY_HOST
rem  Must contain the proxy server address in case you must access the
rem  Internet through a proxy. The default value for the proxy port is 80.
rem  Example: SET PROXY_HOST=proxy.computer.com
rem           SET PROXY_PORT=80
rem ----------------------------------------------------------------------

SET PROXY_HOST=
SET PROXY_PORT=

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
rem  JAVA_VM_OPTS
rem  Java runtime options used
rem ----------------------------------------------------------------------

SET JAVA_VM_OPTS=-Xmx128m

rem ----------------------------------------------------------------------
rem  ZETA_CLASSPATH
rem  Must contain the required libraries, separated by semicolons
rem ----------------------------------------------------------------------

SET ZETA_CLASSPATH=zeta.jar;zeta_client.jar

rem ----------------------------------------------------------------------
rem  ZETA_OPTIONS
rem
rem ----------------------------------------------------------------------

SET ZETA_OPTIONS=-Djava.library.path=.
SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dsun.net.inetaddr.ttl=0
SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.ttl=0
SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.negative.ttl=0

if "%PROXY_HOST%" == "" goto noProxyHost
SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyHost=%PROXY_HOST%
:noProxyHost

if "%PROXY_PORT%" == "" goto noProxyPort
SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyPort=%PROXY_PORT%
:noProxyPort

rem ----------------------------------------------------------------------
rem Executing ZetaGrid client...
rem ----------------------------------------------------------------------

%JAVA_COMMAND% %JAVA_VM_OPTS% -cp %ZETA_CLASSPATH% %ZETA_OPTIONS% zeta.ZetaClient
goto end

rem ----------------------------------------------------------------------
rem Exit
rem ----------------------------------------------------------------------
:error
echo You must set JAVA_HOME to point at your Java development kit installation
:end