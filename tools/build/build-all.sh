#!/bin/sh

hostScript=${0##*/}
scriptDir=${0%/*}
skiptest=false

function usage() {
    printf "$hostScript : Options\n\t-skiptest : Skip unit tests\n\t-help, -h, -? : Show this message\n";
    exit 0
}

while true; do
    case "$1" in
        -skiptest) skiptest=true ;;
        -help | -h | -?) usage ;;
        -*) read -n 1 -p "$hostScript : You've specified an unknown option '$1'. Ignore it and proceed? (y or n) : " ans
            echo ""
            if [ $ans == 'n' ]; then
                exit 1
            fi
            ;;
        *) break ;;
    esac
    shift
done

cd $scriptDir > /dev/null

function checkFile() {
    [ ! -f "$1" ] && printf "%s : [?] can't find the path '%s'\n" $hostScript "$1" && exit 1
}

pushd ../.. > /dev/null

checkFile pom.xml

mvn clean package -Dmaven.test.skip=$skiptest
mvn -f extra-pom.xml compile 
mvn -f zip-pom.xml package 

popd > /dev/null

