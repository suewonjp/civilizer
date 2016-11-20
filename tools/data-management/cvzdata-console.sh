#!/bin/bash

tput sgr 0

hostScript=${0##*/}
scriptDir=${0%/*}
defaultInput="$HOME/.civilizer/database/civilizer"

### Move to the directory where this script is located
cd "$scriptDir"

### Load the script file containing necessary utility functions
utilsDir=$(cd "shell-utils" 2> /dev/null && pwd)
[ "$utilsDir" ] || utilsDir=$(cd "../shell-utils" 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

function help() {
        usage \
            "-db path  : Specify $(tput setaf 1)ABSOLUTE PATH$(tput sgr 0) to Civilizer Database File (.h2.db);" \
            "          : If not specified, it is assumed to be ${defaultInput}" \
            "-shell    : Open H2 Shell instead of H2 Console" \
            "$(tput sgr 0)" \
            "" \
            "EXAMPLE   : ${hostScript} -db ${defaultInput}" \
            "";
}

#[ ! "$1" ] && help

### Open H2 Shell instead of H2 Console when this variable equals 'yes'
withShell=no

### Collect user parameters
while [ "$1" ]; do
    case "$1" in
        -db) inputPath=$2; shift ;;
        -shell) withShell=yes; shift ;;
        -help | -h | -\?) help ;;
    esac
    shift
done

### Set up Java class path to H2 Shell
setupClasspath

### Path to the database
inputPath=${inputPath:-"$defaultInput"}

### Add '.h2.db' prefix if it is missing
[ '.h2.db' != ${inputPath:(-6)} ] && inputPath+=.h2.db

### Confirm that the database file exists; If not, abort the operation
[ ! -f ${inputPath} ] && printf "[ ${hostScript} ]$(tput setaf 1)[ERROR] The database file '${inputPath}' doesn't exist!\n$(tput sgr 0)[ ${hostScript} ] For help, run ${hostScript} -?\n" && exit 1

### Strip the prefix of the database file (Input requirement by H2)
inputPath=${inputPath%.h2.db}

targetTool=Console
[ $withShell = "yes" ] && targetTool=Shell

### Invoke H2 Console or Shell
java -cp $classPath org.h2.tools.${targetTool} -url "jdbc:h2:${inputPath}" -user sa

