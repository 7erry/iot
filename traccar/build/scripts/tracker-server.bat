@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  tracker-server startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and TRACKER_SERVER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\tracker-server.jar;%APP_HOME%\lib\hazelcast-all-4.1-BETA-1.jar;%APP_HOME%\lib\ical4j-2.0.5.jar;%APP_HOME%\lib\calcite-core-1.23.0.jar;%APP_HOME%\lib\jxls-poi-1.0.16.jar;%APP_HOME%\lib\poi-ooxml-4.0.0.jar;%APP_HOME%\lib\poi-4.0.0.jar;%APP_HOME%\lib\calcite-linq4j-1.23.0.jar;%APP_HOME%\lib\avatica-core-1.16.0.jar;%APP_HOME%\lib\httpclient-4.5.9.jar;%APP_HOME%\lib\commons-codec-1.14.jar;%APP_HOME%\lib\h2-1.4.200.jar;%APP_HOME%\lib\mysql-connector-java-8.0.20.jar;%APP_HOME%\lib\postgresql-42.2.14.jar;%APP_HOME%\lib\mssql-jdbc-8.2.2.jre8.jar;%APP_HOME%\lib\HikariCP-3.4.5.jar;%APP_HOME%\lib\ch-smpp-6.0.0-netty4-beta-3.jar;%APP_HOME%\lib\netty-all-4.1.50.Final.jar;%APP_HOME%\lib\slf4j-jdk14-1.7.30.jar;%APP_HOME%\lib\guice-assistedinject-4.2.3.jar;%APP_HOME%\lib\guice-4.2.3.jar;%APP_HOME%\lib\encoder-1.2.2.jar;%APP_HOME%\lib\javax.json-1.1.4.jar;%APP_HOME%\lib\jetty-webapp-9.4.30.v20200611.jar;%APP_HOME%\lib\websocket-server-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-servlet-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-security-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-server-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-jndi-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-proxy-9.4.30.v20200611.jar;%APP_HOME%\lib\jersey-container-servlet-2.31.jar;%APP_HOME%\lib\jersey-media-json-jackson-2.31.jar;%APP_HOME%\lib\jersey-hk2-2.31.jar;%APP_HOME%\lib\jackson-jaxrs-json-provider-2.10.1.jar;%APP_HOME%\lib\jackson-datatype-jsr353-2.10.1.jar;%APP_HOME%\lib\liquibase-core-3.10.1.jar;%APP_HOME%\lib\javax.mail-1.6.2.jar;%APP_HOME%\lib\jxls-2.4.7.jar;%APP_HOME%\lib\velocity-tools-2.0.jar;%APP_HOME%\lib\velocity-1.7.jar;%APP_HOME%\lib\commons-collections4-4.4.jar;%APP_HOME%\lib\jna-platform-5.5.0.jar;%APP_HOME%\lib\jnr-posix-3.0.57.jar;%APP_HOME%\lib\protobuf-java-3.12.2.jar;%APP_HOME%\lib\jaxb-api-2.3.1.jar;%APP_HOME%\lib\jaxb-core-2.3.0.1.jar;%APP_HOME%\lib\jaxb-impl-2.3.3.jar;%APP_HOME%\lib\activation-1.1.1.jar;%APP_HOME%\lib\json-path-2.4.0.jar;%APP_HOME%\lib\janino-3.0.11.jar;%APP_HOME%\lib\commons-compiler-3.0.11.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.12.jar;%APP_HOME%\lib\ch-commons-charset-3.0.2.jar;%APP_HOME%\lib\ch-commons-util-6.0.2.jar;%APP_HOME%\lib\avatica-metrics-1.16.0.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\guava-27.1-jre.jar;%APP_HOME%\lib\checker-compat-qual-2.0.0.jar;%APP_HOME%\lib\error_prone_annotations-2.2.0.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.17.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\websocket-servlet-9.4.30.v20200611.jar;%APP_HOME%\lib\javax.servlet-api-3.1.0.jar;%APP_HOME%\lib\websocket-client-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-client-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-http-9.4.30.v20200611.jar;%APP_HOME%\lib\websocket-common-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-io-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-xml-9.4.30.v20200611.jar;%APP_HOME%\lib\jetty-util-9.4.30.v20200611.jar;%APP_HOME%\lib\jersey-container-servlet-core-2.31.jar;%APP_HOME%\lib\jersey-server-2.31.jar;%APP_HOME%\lib\jersey-client-2.31.jar;%APP_HOME%\lib\jersey-media-jaxb-2.31.jar;%APP_HOME%\lib\jersey-common-2.31.jar;%APP_HOME%\lib\jersey-entity-filtering-2.31.jar;%APP_HOME%\lib\jakarta.ws.rs-api-2.1.6.jar;%APP_HOME%\lib\jackson-module-jaxb-annotations-2.10.1.jar;%APP_HOME%\lib\jackson-jaxrs-base-2.10.1.jar;%APP_HOME%\lib\jackson-databind-2.10.1.jar;%APP_HOME%\lib\jackson-annotations-2.10.1.jar;%APP_HOME%\lib\hk2-locator-2.6.1.jar;%APP_HOME%\lib\javassist-3.25.0-GA.jar;%APP_HOME%\lib\jackson-core-2.10.1.jar;%APP_HOME%\lib\javax.json-api-1.0.jar;%APP_HOME%\lib\commons-jexl-2.1.1.jar;%APP_HOME%\lib\struts-taglib-1.3.8.jar;%APP_HOME%\lib\struts-tiles-1.3.8.jar;%APP_HOME%\lib\struts-core-1.3.8.jar;%APP_HOME%\lib\commons-chain-1.1.jar;%APP_HOME%\lib\commons-validator-1.3.1.jar;%APP_HOME%\lib\commons-digester-1.8.jar;%APP_HOME%\lib\commons-beanutils-1.9.2.jar;%APP_HOME%\lib\logback-core-1.1.3.jar;%APP_HOME%\lib\commons-collections-3.2.1.jar;%APP_HOME%\lib\commons-lang-2.4.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\dom4j-1.1.jar;%APP_HOME%\lib\oro-2.0.8.jar;%APP_HOME%\lib\sslext-1.2-0.jar;%APP_HOME%\lib\commons-lang3-3.8.jar;%APP_HOME%\lib\threetenbp-1.3.6.jar;%APP_HOME%\lib\jna-5.5.0.jar;%APP_HOME%\lib\jnr-ffi-2.1.15.jar;%APP_HOME%\lib\jnr-constants-0.9.15.jar;%APP_HOME%\lib\javax.activation-api-1.2.0.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.3.jar;%APP_HOME%\lib\jakarta.activation-1.2.2.jar;%APP_HOME%\lib\httpcore-4.4.11.jar;%APP_HOME%\lib\json-smart-2.3.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\checker-qual-2.5.2.jar;%APP_HOME%\lib\websocket-api-9.4.30.v20200611.jar;%APP_HOME%\lib\hk2-api-2.6.1.jar;%APP_HOME%\lib\hk2-utils-2.6.1.jar;%APP_HOME%\lib\jakarta.inject-2.6.1.jar;%APP_HOME%\lib\jakarta.annotation-api-1.3.5.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.3.jar;%APP_HOME%\lib\jakarta.validation-api-2.0.2.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.2.jar;%APP_HOME%\lib\aopalliance-repackaged-2.6.1.jar;%APP_HOME%\lib\poi-ooxml-schemas-4.0.0.jar;%APP_HOME%\lib\commons-compress-1.18.jar;%APP_HOME%\lib\curvesapi-1.04.jar;%APP_HOME%\lib\antlr-2.7.2.jar;%APP_HOME%\lib\joda-time-2.10.6.jar;%APP_HOME%\lib\jffi-1.2.23.jar;%APP_HOME%\lib\jffi-1.2.23-native.jar;%APP_HOME%\lib\asm-commons-7.1.jar;%APP_HOME%\lib\asm-util-7.1.jar;%APP_HOME%\lib\asm-analysis-7.1.jar;%APP_HOME%\lib\asm-tree-7.1.jar;%APP_HOME%\lib\accessors-smart-1.2.jar;%APP_HOME%\lib\asm-7.1.jar;%APP_HOME%\lib\jnr-a64asm-1.0.0.jar;%APP_HOME%\lib\jnr-x86asm-1.0.2.jar;%APP_HOME%\lib\xmlbeans-3.0.1.jar

@rem Execute tracker-server
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %TRACKER_SERVER_OPTS%  -classpath "%CLASSPATH%" org.traccar.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable TRACKER_SERVER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%TRACKER_SERVER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
