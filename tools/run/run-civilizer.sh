#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

cd "$scriptDir"

utilsDir=$(cd "shell-utils" 2> /dev/null && pwd)
[ "$utilsDir" ] || utilsDir=$(cd "../shell-utils" 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

home=
port=8080
cleanStart=
while true; do
    case "$1" in
        -port) port=$2 ;;
        -home) home=$2 ;;
        -cleanStart) cleanStart=--cleanStart ;;
        -help | -h | -\?) usage \
            "-port number : Specify port number" \
            "-home path : Specify Private Home Directory" \
            "-cleanStart : Start the app with a clean empty DB" \
            "        [CAUTION!!!] Your previous data will be all gone. Make a backup first!" \
            ;;
        -*) unknownArg $1 ;;
        '') break ;;
    esac
    shift
done

setupClasspath

cd "$extraPath/../"
if [ -f "../pom.xml" ]; then 
    cd ..
fi

echo "[ $hostScript ] : Loading Civilizer..."
java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog \
 -Dorg.eclipse.jetty.LEVEL=INFO \
 -cp "$classPath" com.civilizer.extra.tools.Launcher "$cleanStart" --port $port --home "$home"
