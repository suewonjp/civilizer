#!/usr/bin/env bash

hostScript=${0##*/}
scriptDir=${0%/*}
defaultInput="$HOME/.civilizer/database/civilizer"

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

help() {
        usage \
            "-db path  : Specify $(echolor $bold $red ABSOLUTE PATH) to Civilizer Database File (.h2.db);" \
            "          : If not specified, it is assumed to be ${defaultInput}" \
            "-shell    : Open H2 Shell instead of H2 Console" \
            "" \
            "EXAMPLE   : ${hostScript} -db ${defaultInput}" \
            "";
}

#[ ! "$1" ] && help

### Open H2 Shell instead of H2 Console when this variable equals 'yes'
withShell=no

### Collect user parameters
debug=
while [ "$1" ]; do
    case "$1" in
        -db) inputPath=$2; shift ;;
        -shell) withShell=yes; ;;
        -debug) debug=debug ;;
        -help | -h | -\?) help ;;
        -*) onUnknownOption $1 ;;
        *) break ;;
    esac
    shift
done

### Set up Java class path to H2 Shell
setupClasspath "$debug"

### Path to the database
inputPath="${inputPath:-"$defaultInput"}"

### Add '.h2.db' prefix if it is missing
[ '.h2.db' != "${inputPath:(-6)}" ] && inputPath+=.h2.db

### Confirm that the database file exists; If not, abort the operation
[ ! -f "${inputPath}" ] && \
if [ ! -f "${inputPath}" ]; then
    printf "[ %s ][ %s ] The database file '%s' doesn't exist!\n[ %s ] For help, run %s\n" \
        "${hostScript}" "$(echolor $bold $magenta ERROR)" \
        "${inputPath}" \
        "${hostScript}" "$(echolor $reverse $cyan "${hostScript}" -?)" && \
    exit 1
fi

### Strip the prefix of the database file (Input requirement by H2)
inputPath="${inputPath%.h2.db}"

targetTool=Console
[ $withShell = "yes" ] && targetTool=Shell

if [ "$debug" ]; then
    printvar withShell
    printvar targetTool
    printvar inputPath
    printvar classPath
fi

### Invoke H2 Console or Shell
echo
${debug:+echo} java -cp "$classPath" "org.h2.tools.${targetTool}" -url "jdbc:h2:${inputPath}" -user sa

