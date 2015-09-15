#!/bin/sh

hostScript=${0##*/}
scriptDir=${0%/*}

cd $scriptDir

utilsDir=$(cd shell-utils 2> /dev/null && pwd)
[ $utilsDir ] || utilsDir=$(cd ../shell-utils 2> /dev/null && pwd)
PATH=$utilsDir:$PATH
source "commons.sh"

skiptest=false
while true; do
    case "$1" in
        -skiptest) skiptest=true ;;
        -help | -h | -\?) usage "-skiptest: Skip unit tests" ;;
        -*) onUnknownArg $1 ;;
        *) break ;;
    esac
    shift
done

pushd ../.. > /dev/null

checkPath pom.xml

### This is a main build which creates .WAR file
### Basically this .war file is enough to run Civilizer
mvn clean package -Dmaven.test.skip=$skiptest

### This task compiles extra binaries 
### such as Offline data exporter/importer and Launcher and
### resolves the dependency for Jetty Web server.
mvn -f extra-pom.xml compile 

### This task compresses all files into the final .zip package
mvn -f zip-pom.xml package 

popd > /dev/null

