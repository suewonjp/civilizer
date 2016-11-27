#!/bin/bash

function printVar() {
    printf '[ %s DEBUG ] $%s = %s\n' $hostScript $1 ${!1}
}

function usage() {
    printf "[ %s ] Options\n" $hostScript
    printf "\t%s\n" "$@"
    printf "\t-help, -h, -? : Show this message\n"
    exit 0
}

function onUnknownArg() {
    read -n 1 -p "[ $hostScript ] : You've specified an unknown option '$1'. Ignore it and proceed? (y or n) :" ans
    echo ""
    if [ $ans == 'n' ]; then
        exit 1
    fi
}

function checkPath() {
    [ -e "$1" ] || { printf "[ $hostScript ERROR ] Can't access the path \"%s\"\n" $1 && exit 1; }
}

function setupClasspath() {
    webappPath=civilizer
    extraPath=extra
    extraLibPath=$extraPath/lib
    if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f "$extraLibPath/jetty-runner.jar" ]; then
        webappPath=../../target/civilizer-1.0.0.CI-SNAPSHOT
        extraPath=../../target/extra
        extraLibPath=../../extra/lib

        if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f "$extraLibPath/jetty-runner.jar" ]; then
            echo "[ $hostScript ERROR ] Civilizer can't be found!"
            exit 1
        fi
    fi

    webappPath=$( cd "$webappPath" && pwd )
    extraPath=$( cd "$extraPath" && pwd )
    classPath=$webappPath/WEB-INF/classes:$extraLibPath/*:$webappPath/WEB-INF/lib/*:$extraPath

    PREV_IFS=$IFS
    IFS=":*"
    for cp in $classPath; do
        [ $cp ] && checkPath $cp
    done
    IFS=$PREV_IFS

    [ $1 -a $1 == "print" ] && {
        printVar webappPath; 
        printVar extraPath; 
        printVar classPath;
    }

    local uname=$( uname )
    if [ ${uname:0:6} = "CYGWIN" ]; then
        classPath=$( cygpath -pw "$classPath" )
    fi
}

