@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

cd "%scriptDir%"

set importPath=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-from] set importPath=%2
    if [%1] == [-home] set home=%2
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" call "..\shell-utils\classpath.bat"
if exist "shell-utils\classpath.bat" call "shell-utils\classpath.bat" 

set homeOption=
if not [%home%] == [] set homeOption="-Dcivilizer.private_home_path=%home%"

cd "!extraPath!\.."
echo [ %hostScript% ] Importing Data...
java -cp "%classPath%" %homeOption% com.civilizer.extra.tools.DataBroker -import %importPath%
 
:: Everything is OK... :-)
goto :eof

:usage
    echo [ %hostScript% ] Options
    echo     -from path : Specify path to the FILE to import
    echo     -home path : Specify Private Home Directory

endlocal
