#!/bin/sh

hostScript=${0##*/}
scriptDir=${0%/*}
home=
port=8080

function usage() {
    printf "$hostScript : Options\n \
        \t-port number: Specify port number\n \
        \t-home path: Specify private home path\n \
        \t-help, -h, -? : Show this message\n";
    exit 0
}

while true; do
    case "$1" in
        -port | --port) shift; port=$1 ;;
        -home | --home) shift; home=$1 ;;
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

webappPath=civilizer
extraPath=extra
if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f $extraPath/lib/jetty-runner.jar ]; then
    webappPath=../../target/civilizer-1.0.0.CI-SNAPSHOT
    extraPath=../../target/extra
    if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f $extraPath/lib/jetty-runner.jar ]; then
        echo "$hostScript : [?] Civilizer can't be found!"
        exit 1
    fi
fi

webappPath=$( cd "$webappPath"; pwd )
extraPath=$( cd "$extraPath"; pwd )
homeOption=${home:+-Dcivilizer.private_home_path="$home"}
classPath="$webappPath/WEB-INF/classes:$extraPath/lib/*:$webappPath/WEB-INF/lib/*:$extraPath"

PREV_IFS=$IFS
IFS=":*"
for cp in $classPath; do
    [ -e $cp ] || { printf "$hostScript : [?] Can't access the classpath \"%s\"\n" $cp && exit 1; }
done
IFS=$PREV_IFS

#printf '$webappPath = %s\n' $webappPath
#printf '$extraPath = %s\n' $extraPath
#printf '$classPath = %s\n' $classPath
#printf '$homeOption = %s\n' $homeOption

cd $extraPath/../ > /dev/null
echo "$hostScript : Loading Civilizer..."
java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog \
 -Dorg.eclipse.jetty.LEVEL=INFO \
 -cp "$classPath" $homeOption com.civilizer.extra.tools.Launcher --port $port
