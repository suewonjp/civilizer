#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

cd $scriptDir

utilsDir=$(cd shell-utils 2> /dev/null && pwd)
[ $utilsDir ] || utilsDir=$(cd ../shell-utils 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

exportPath=

while true; do
    case "$1" in
        -to) exportPath=$2 ;;
        -help | -h | -\?) usage "-to path : Specify output path" ;;
        -*) onUnknownArg $1 ;;
        *) break ;;
    esac
    shift
done

setupClasspath 

cd $extraPath/../

echo "[ $hostScript ] : Exporting Data..."
java -cp $classPath com.civilizer.extra.tools.DataBroker -export $exportPath

