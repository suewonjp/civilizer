@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0

call :confirmJre
if %errorlevel% neq 0 exit /b 1

cd "%scriptDir%"

set home=
set port=8080
set cleanStart=
set debug=
:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-port] set port=%2
    if [%1] == [-home] set home=%2
    if [%1] == [-cleanStart] set cleanStart=--cleanStart
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

cd "!extraPath!\.."
if exist "..\pom.xml" cd ".."

echo [ %hostScript% ] Loading Civilizer...
!java! -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog ^
  -Dorg.eclipse.jetty.LEVEL=INFO ^
  -Dfile.encoding=UTF8 ^
  -cp "%classPath%" ^
  com.civilizer.extra.tools.Launcher %cleanStart% --port %port% --home %home%
 
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
    echo     -port number : Specify port number
    echo     -home path   : Specify Private Home Directory
    echo     -cleanStart  : Start the app with a clean empty DB
    echo                    [CAUTION^^!^^!^^!] Your previous data will be all gone. Make a backup first^^! 

endlocal
