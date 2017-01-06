#!/bin/bash

hostScript=${0##*/}
scriptDir=${0%/*}

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

home="$HOME/.civilizer"
importPath=
debug=
while true; do
    case "$1" in
        -from) importPath=$2 ;;
        -home) home=$2 ;;
        -debug) debug=debug ;;
        -help | -h | -\?) usage \
            "-from path : $(echolor $bold $red [REQUIRED]) Specify FILE to import as an $(echolor $bold $magenta ABSOLUTE ) path" \
            "-home path : Specify Private Home Directory (default is ~/.civilizer)" \
            ;;
        -*) onUnknownOption $1 ;;
        '') break ;;
    esac
    shift
done

setupClasspath "$debug"

homeOption=-Dcivilizer.private_home_path="$home"

cd "$extraPath/../"

echo "[ $hostScript ] : Importing Data..."
${debug:+echo} java "$homeOption" -cp "$classPath" "com.civilizer.extra.tools.DataBroker" -import "$importPath"

