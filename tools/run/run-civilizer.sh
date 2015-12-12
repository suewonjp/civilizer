#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

cd "$scriptDir"

utilsDir=$(cd "shell-utils" 2> /dev/null && pwd)
[ "$utilsDir" ] || utilsDir=$(cd "../shell-utils" 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

home=
port=8080
while true; do
    case "$1" in
        -port) port=$2 ;;
        -home) home=$2 ;;
        -help | -h | -\?) usage \
            "-port number : Specify port number" \
            "-home path : Specify Private Home Directory" \
            ;;
        -*) unknownArg $1 ;;
        '') break ;;
    esac
    shift
done

setupClasspath 

homeOption=${home:+-Dcivilizer.private_home_path="$home"}

cd "$extraPath/../"

echo "[ $hostScript ] : Loading Civilizer..."
java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog \
 -Dorg.eclipse.jetty.LEVEL=INFO \
 -cp "$classPath" "$homeOption" com.civilizer.extra.tools.Launcher --port $port
