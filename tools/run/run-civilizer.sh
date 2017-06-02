#!/usr/bin/env bash

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

confirmJre

### Collect user parameters
home=
port=8080
cleanStart=
debug=
while true; do
    case "$1" in
        -port) port=$2; shift ;;
        -home) home=$2; shift ;;
        -cleanStart) cleanStart=--cleanStart ;;
        -debug) debug=debug ;;
        -help | -h | -\?) usage \
            "-port number : Specify port number" \
            "-home path : Specify Private Home Directory" \
            "-cleanStart : Start the app with a clean empty DB" \
            "        $(echolor $bold $magenta '[CAUTION!!!] Your previous data will be all gone. Make a backup first!')" \
            ;;
        -*) onUnknownOption $1 ;;
        *) break ;;
    esac
    shift
done

### Set up Java class path
setupClasspath "$debug"

cd "$extraPath/../"

### If the script runs at root of the source package,
### we need to adjust the working directory
[ -f "../pom.xml" ] && cd ..

### Everything is ready for running Civilizer
echo "[ $hostScript ] : Loading Civilizer from $(echolor $reverse $yellow $PWD)..."
${debug:+echo} java -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog \
 -Dorg.eclipse.jetty.LEVEL=INFO \
 -Dfile.encoding=UTF8 \
 -cp "$classPath" "com.civilizer.extra.tools.Launcher" "$cleanStart" --port $port --home "$home"

