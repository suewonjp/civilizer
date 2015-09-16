
set webappPath=civilizer
set extraPath=extra
if not exist "!webappPath!\WEB-INF\web.xml" (
    if not exist "!extraPath!\lib\jetty-runner.jar" (
        set webappPath=..\..\target\civilizer-1.0.0.CI-SNAPSHOT
        set extraPath=..\..\target\extra
        if not exist "!webappPath!\WEB-INF\web.xml" (
            if not exist "!extraPath!\lib\jetty-runner.jar" (
                echo "[%hostScript% ERROR] Civilizer can't be found!"
                exit /b 1
            )
        )        
    )
)

call :toAbsolutePath webappPath 
call :toAbsolutePath extraPath

set classPath="!webappPath!\WEB-INF\classes;!extraPath!\lib\*;!webappPath!\WEB-INF\lib\*;!extraPath!"

::echo webappPath = !webappPath!
::echo extraPath = !extraPath!
::echo classPath = !classPath!

goto :eof

:toAbsolutePath
    call pushd %%%1%%
    set %1=%cd%
    popd
    exit /b 0
