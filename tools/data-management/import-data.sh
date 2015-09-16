#!/bin/sh

hostScript=${0##*/}
scriptDir=${0%/*}

cd $scriptDir

utilsDir=$(cd shell-utils 2> /dev/null && pwd)
[ $utilsDir ] || utilsDir=$(cd ../shell-utils 2> /dev/null && pwd)
PATH=$utilsDir:$PATH
source "commons.sh"

importPath=

while true; do
    case "$1" in
        -from) importPath=$2 ;;
        -help | -h | -\?) usage "-from path : Specify input path" ;;
        -*) onUnknownArg $1 ;;
        *) break ;;
    esac
    shift
done

setupClasspath 

cd $extraPath/../

echo "[ $hostScript ] : Importing Data..."
java -cp $classPath com.civilizer.extra.tools.DataBroker -import $importPath

