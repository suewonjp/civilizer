@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0

if [%1] == [-help] goto usage
if [%1] == [-h] goto usage
if [%1] == [-?] goto usage

set skiptest=false
if [%1] == [-skiptest] set skiptest=true 

pushd %~dp0..\..
    call :checkPath pom.xml
    if %errorlevel% neq 0 exit /b 1

    call mvn clean package -Dmaven.test.skip=!skiptest!
    call mvn -f extra-pom.xml compile
    call mvn -f zip-pom.xml package
popd

:: Everything is OK... :-)
goto :eof


:checkPath
    if not exist %1 (
        echo [ %hostScript% ERROR ] can't find the path '%cd%\%1'
        exit /b 1
    )
    exit /b 0
    
:usage
    echo [ %hostScript% ] Options
    echo     -skiptest : Skip unit tests
    echo     -help or -h or -? : Show this message

endlocal
