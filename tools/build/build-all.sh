#!/usr/bin/env bash

confirmPrerequisite() {
    type javac > /dev/null 2>&1 || {
        printf "[ %s ][ %s ] Can't find JDK (Java Development Kit)!\n" "$hostScript" "$(echolor $bold $magenta ERROR)"
        printf "\t( You need to install JDK/Maven to build Civilizer using this script! )\n"
        exit 1
    }

    type mvn > /dev/null 2>&1 || {
        printf "[ %s ][ %s ] Can't find Maven!\n" "$hostScript" "$(echolor $bold $magenta ERROR)"
        printf "\t( You need to install Maven to build Civilizer using this script! )\n"
        exit 1
    }
}

hostScript=${0##*/}
scriptDir=${0%/*}

### Move to the directory where this script is located
cd "$scriptDir"

### Load the script file containing necessary utility functions
[ -f "shell-utils/commons.sh" ] && source "shell-utils/commons.sh" || {
    [ -f "../shell-utils/commons.sh" ] && source "../shell-utils/commons.sh";
} || {
    echo "[ $hostScript ][ ERROR ] Can't find shell-utils/common.sh!"
    echo -e "\t ( You may be running the script from a wrong place... )"
    exit 1
}

### Confirm required tools are installed
confirmPrerequisite

### Collect user parameters
skiptest=false
debug=
while true; do
    case "$1" in
        -skiptest) skiptest=true ;;
        -debug) debug=debug ;;
        -help | -h | -\?) usage "-skiptest : Skip unit tests" ;;
        -*) onUnknownOption $1 ;;
        *) break ;;
    esac
    shift
done

cd "../.."

### Are we at the root of the source package?
checkPath pom.xml

### This is a main build which creates .WAR file
### Basically this .war file is enough to run Civilizer
${debug:+echo} mvn clean package -Dmaven.test.skip=$skiptest

### This task compiles extra binaries 
### such as Offline data exporter/importer and Launcher and
### resolves the dependency for Jetty Web server.
${debug:+echo} mvn -f extra-pom.xml compile

### This task compresses all files into the final .zip package
${debug:+echo} mvn -f zip-pom.xml package

