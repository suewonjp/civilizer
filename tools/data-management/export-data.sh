#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

cd "$scriptDir"

utilsDir=$(cd "shell-utils" 2> /dev/null && pwd)
[ "$utilsDir" ] || utilsDir=$(cd "../shell-utils" 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

home=
exportPath=
while true; do
    case "$1" in
        -to) exportPath=$2 ;;
        -home) home=$2 ;;
        -help | -h | -\?) usage \
            "-to path : Specify OUTPUT FOLDER path" \
            "-home path : Specify Private Home Directory" \
            ;;
        -*) onUnknownArg $1 ;;
        '') break ;;
    esac
    shift
done

setupClasspath

homeOption=${home:+-Dcivilizer.private_home_path="$home"}

cd "$extraPath/../"

echo "[ $hostScript ] : Exporting Data..."
java -cp $classPath "$homeOption" com.civilizer.extra.tools.DataBroker -export $exportPath

