@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

cd %scriptDir%

set importPath=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-from] set importPath=%2
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" call "..\shell-utils\classpath.bat"
if exist "shell-utils\classpath.bat" call "shell-utils\classpath.bat" 

cd !extraPath!\..
echo [ %hostScript% ] Importing Data...
java -cp %classPath% com.civilizer.extra.tools.DataBroker -import %importPath%
 
:: Everything is OK... :-)
goto :eof

:usage
    echo [ %hostScript% ] Options
    echo     -from path : Specify input path
    echo     -help or -h or -? : Show this message

endlocal
