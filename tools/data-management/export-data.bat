@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

cd %scriptDir%

set exportPath=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-to] set exportPath=%2
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" call "..\shell-utils\classpath.bat"
if exist "shell-utils\classpath.bat" call "shell-utils\classpath.bat" 

cd !extraPath!\..
echo [ %hostScript% ] Exporting Data...
java -cp %classPath% com.civilizer.extra.tools.DataBroker -export %exportPath%
 
:: Everything is OK... :-)
goto :eof

:usage
    echo [ %hostScript% ] Options
    echo     -to path : Specify output path
    echo     -help or -h or -? : Show this message

endlocal
