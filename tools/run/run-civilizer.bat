@echo off

rem HideSelf

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

cd "%scriptDir%"

set home=
set port=8080
set cleanStart=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-port] set port=%2
    if [%1] == [-home] set home=%2
    if [%1] == [-cleanStart] set cleanStart=--cleanStart
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" call "..\shell-utils\classpath.bat"
if exist "shell-utils\classpath.bat" call "shell-utils\classpath.bat" 
::echo !webappPath!
::echo !extraPath!
::echo !classPath!

cd "!extraPath!\.."
if exist "..\pom.xml" cd ..

echo [ %hostScript% ] Loading Civilizer...
java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog ^
 -Dorg.eclipse.jetty.LEVEL=INFO ^
 -cp "%classPath%" com.civilizer.extra.tools.Launcher %cleanStart% --port %port% --home %home%
 
:: Everything is OK... :-)
goto :eof

:usage
    echo [ %hostScript% ] Options
    echo     -port number : Specify port number
    echo     -home path   : Specify Private Home Directory
    echo     -cleanStart  : Start the app with a clean empty DB
    echo                    [CAUTION^^!^^!^^!] Your previous data will be all gone. Make a backup first^^! 

endlocal
