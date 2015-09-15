#!/bin/sh

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

function setupClasspath() {
    webappPath=civilizer
    extraPath=extra
    if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f $extraPath/lib/jetty-runner.jar ]; then
        webappPath=../../target/civilizer-1.0.0.CI-SNAPSHOT
        extraPath=../../target/extra

        if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f $extraPath/lib/jetty-runner.jar ]; then
            echo "[ $hostScript ERROR ] Civilizer can't be found!"
            exit 1
        fi
    fi

    webappPath=$( cd "$webappPath" && pwd )
    extraPath=$( cd "$extraPath" && pwd )
    classPath="$webappPath/WEB-INF/classes:$extraPath/lib/*:$webappPath/WEB-INF/lib/*:$extraPath"   

    PREV_IFS=$IFS
    IFS=":*"
    for cp in $classPath; do
        [ -e $cp ] || { printf "[ $hostScript ERROR ] Can't access the classpath \"%s\"\n" $cp && exit 1; }
    done
    IFS=$PREV_IFS

    [ $1 -a $1 == "print" ] && {
        printVar webappPath; 
        printVar extraPath; 
        printVar classPath;
    }
}

