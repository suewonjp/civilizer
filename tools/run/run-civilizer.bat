@echo off

setlocal EnableDelayedExpansion

set hostScript=%~nx0
set scriptDir=%~dp0
set home=
set port=8080

:param_setup
    if [%1] == [-help] goto usage
    if [%1] == [-h] goto usage
    if [%1] == [-?] goto usage
    if [%1] == [-port] set port=%2
    if [%1] == [-home] set home=%2
    shift
    if not [%1] == [] goto param_setup

:run

cd %scriptDir%

set webappPath=civilizer
set extraPath=extra
if not exist "!webappPath!\WEB-INF\web.xml" (
    if not exist "!extraPath!\lib\jetty-runner.jar" (
        set webappPath=..\..\target\civilizer-1.0.0.CI-SNAPSHOT
        set extraPath=..\..\target\extra
        if not exist "!webappPath!\WEB-INF\web.xml" (
            if not exist "!extraPath!\lib\jetty-runner.jar" (
                echo "%hostScript% : [?] Civilizer can't be found!"
                exit /b 1
            )
        )        
    )
)

call :toAbsolutePath !webappPath! webappPath
call :toAbsolutePath !extraPath! extraPath

set homeOption=
if not [%home%] == [] set homeOption=Dcivilizer.private_home_path="%home%"

set classPath="%webappPath%\WEB-INF\classes;%extraPath%\lib\*;%webappPath%\WEB-INF\lib\*;%extraPath%"

::echo !webappPath!
::echo !extraPath!
::echo !homeOption!
::echo !classPath!

cd !extraPath!\..
echo %hostScript% : Loading Civilizer...
java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog ^
 -Dorg.eclipse.jetty.LEVEL=INFO ^
 -cp %classPath% %homeOption% com.civilizer.extra.tools.Launcher --port %port%
 
:: Everything is OK... :-)
goto :eof

:toAbsolutePath
    pushd %1
        set %2=%cd%
    popd
    exit /b 0

:usage
    echo %hostScript% :  Options
    echo     -skiptest : Skip unit tests
    echo     -help or -h or -? : Show this message

endlocal
