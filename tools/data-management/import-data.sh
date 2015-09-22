#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

cd $scriptDir

utilsDir=$(cd shell-utils 2> /dev/null && pwd)
[ $utilsDir ] || utilsDir=$(cd ../shell-utils 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

home=
importPath=
while true; do
    case "$1" in
        -from) importPath=$2 ;;
        -home) home=$2 ;;
        -help | -h | -\?) usage \
            "-from path : Specify path to the FILE to import" \
            "-home path : Specify Private Home Directory" \
            ;;
        -*) onUnknownArg $1 ;;
        '') break ;;
    esac
    shift
done

setupClasspath

homeOption=${home:+-Dcivilizer.private_home_path="$home"}

cd $extraPath/../

echo "[ $hostScript ] : Importing Data..."
java -cp $classPath $homeOption com.civilizer.extra.tools.DataBroker -import $importPath

