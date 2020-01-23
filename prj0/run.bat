@echo off
setlocal ENABLEDELAYEDEXPANSION

set JAVA_HOME=jre
set JAR_HOME=lib
set CLASSPATH=.
set MAIN_CLASS=com.dma.nicomains.Main_CA_Parser_Encodage

for /f "tokens=*" %%a in ('dir /b /a-d %JAR_HOME%\*.jar') do ( 
	set JAR=%%a
	set CLASSPATH=!CLASSPATH!;%JAR_HOME%/!JAR!
)

"%JAVA_HOME%/bin/java" -cp %CLASSPATH% %MAIN_CLASS% %*
