@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

cd "%scriptDir%"

set home=
set exportPath=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-to] set exportPath=%2
    if [%1] == [-home] set home=%2
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" call "..\shell-utils\classpath.bat"
if exist "shell-utils\classpath.bat" call "shell-utils\classpath.bat" 

set homeOption=
if not [%home%] == [] set homeOption=-Dcivilizer.private_home_path=%home%

cd "!extraPath!\.."
echo [ %hostScript% ] Exporting Data...
java -cp "%classPath%" "%homeOption%" com.civilizer.extra.tools.DataBroker -export %exportPath%
 
:: Everything is OK... :-)
goto :eof

:usage
    echo [ %hostScript% ] Options
    echo     -to path : Specify OUTPUT FOLDER path
    echo     -home path : Specify Private Home Directory

endlocal
