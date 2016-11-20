#!/bin/bash

tput sgr 0

hostScript=${0##*/}
scriptDir=${0%/*}
curDir=$PWD
defaultInput="~/.civilizer/database/civilizer"

### Move to the directory where this script is located
cd "$scriptDir"

### Load the script file containing necessary utility functions
utilsDir=$(cd "shell-utils" 2> /dev/null && pwd)
[ "$utilsDir" ] || utilsDir=$(cd "../shell-utils" 2> /dev/null && pwd)
source "$utilsDir/commons.sh"

function help() {
        usage \
            "-from path   : Specify $(tput setaf 1)ABSOLUTE PATH$(tput sgr 0) to Civilizer Database File (.h2.db);" \
            "             : If not specified, it is assumed to be ${defaultInput}" \
            "-to path     : Specify $(tput setaf 1)ABSOLUTE PATH$(tput sgr 0) to Output Directory;" \
            "             : If not specified, it is assumed to be the Current Working Directory" \
            "   table ... : [REQUIRED] Specify Name of Tables to Export; (multiple tables are accepted)" \
            "             : Following tables are available (case insensitive)$(tput setaf 2)" \
            "                 FILE"               \
            "                 FRAGMENT"           \
            "                 FRAGMENT2FRAGMENT"  \
            "                 GLOBAL_SETTING"     \
            "                 TAG"                \
            "                 TAG2FRAGMENT"       \
            "                 TAG2TAG$(tput sgr 0)" \
            "" \
            "    EXAMPLE  : ${hostScript} -from ${defaultInput} -to ${curDir} fragment tag tag2fragemnt" \
            "";
}

[ ! "$1" ] && help

### Array to contain the name of tables to export
unset tables

### Collect user parameters
while [ "$1" ]; do
    case "$1" in
        -from) inputPath=$2; shift ;;
        -to) outputDir=$2; shift ;;
        -help | -h | -\?) help ;;
        *) tables+=($1) ;;
    esac
    shift
done

### Set up Java class path to H2 Shell
setupClasspath

### Table names are minimum required parameters
[ ! ${tables} ] && printf "[ ${hostScript} ]$(tput setaf 1)[ERROR] Table names are required!\n$(tput sgr 0)[ ${hostScript} ] For help, run ${hostScript} -?\n" && exit 1

### Path to the database
inputPath=${inputPath:-"$HOME/.civilizer/database/civilizer"}

### Add '.h2.db' prefix if it is missing
[ '.h2.db' != ${inputPath:(-6)} ] && inputPath+=.h2.db

### Confirm that the database file exists; If not, abort the operation
[ ! -f ${inputPath} ] && printf "[ ${hostScript} ]$(tput setaf 1)[ERROR] The database file '${inputPath}' doesn't exist!\n$(tput sgr 0)[ ${hostScript} ] For help, run ${hostScript} -?\n" && exit 1

### Strip the prefix of the database file (Input requirement by H2)
inputPath=${inputPath%.h2.db}

### Output path will be the current working directory, if not specified by the user
outputDir=${outputDir:-"$curDir"}

### Export the content of each database table to a .CSV file one by one
for t in ${tables[@]}; do
    tput sgr 0
    outputCsv="${outputDir}/${t}-`date \"+%y-%m-%d_%Hh%Mm%Ss\"`.csv"
    #echo outputDir ___ ${outputCsv}

    command="call csvwrite ( '${outputCsv}', 'select * from ${t}' )"
    #echo command ___  ${command}

    ### Export data using H2 Shell
    ### For more details, visit http://www.h2database.com/javadoc/org/h2/tools/Shell.html
    java -cp $classPath org.h2.tools.Shell -url "jdbc:h2:${inputPath}" -user sa -sql "${command}"

    [ -f "${outputCsv}" ] && printf "[ ${hostScript} ] Table '${t}' has been exported to $(tput setaf 6)${outputCsv}\n\n"
done

