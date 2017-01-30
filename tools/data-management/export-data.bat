@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

call :confirmJre
if %errorlevel% neq 0 exit /b 1

cd "%scriptDir%"

set home=%userprofile%\.civilizer
set exportPath=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-to] set exportPath=%2
    if [%1] == [-home] set home=%2
    if [%1] == [-debug] set debug=debug
    shift
    if not [%1] == [] goto param_setup
    
if exist "..\shell-utils\classpath.bat" (
    call "..\shell-utils\classpath.bat" %debug%
) else (
    if exist "shell-utils\classpath.bat" (
        call "shell-utils\classpath.bat" %debug%
    ) else (
        echo [ %hostScript% ][ ERROR ] Can't find shell-utils\classpath.bat^!
        echo     ^( You may be running the script from a wrong place... ^)
        exit 1
    )
)

set java=java
if [%debug%] == [debug] (
    set java=echo java
)

set homeOption=-Dcivilizer.private_home_path=%home%

cd "!extraPath!\.."
echo [ %hostScript% ] Exporting Data...
!java! %homeOption% -cp "%classPath%" ^
  com.civilizer.extra.tools.DataBroker -export %exportPath%
 
:: Everything is OK... :-)
goto :eof

:confirmJre
    where java >nul 2>&1 || (
        echo [ %hostScript% ][ ERROR ] can't find JRE ^(Java Runtime Environment^)^!
        echo     ^( Downlaod and install JRE from Oracle ^)
        exit /b 1
    )
    exit /b 0

:usage
    echo [ %hostScript% ] Options
    echo     -to path : Specify OUTPUT FOLDER as an ABSOLUTE path
    echo     -home path : Specify Private Home Directory ^(default is %userprofile%\.civilizer^)

endlocal
