#!/usr/bin/env bash

hostScript=${0##*/}
scriptDir=${0%/*}
curDir="$PWD"
defaultInput="~/.civilizer/database/civilizer"

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
            "-from path   : Specify $(echolor $bold $red ABSOLUTE PATH) to Civilizer Database File (.h2.db);" \
            "             : If not specified, it is assumed to be ${defaultInput}" \
            "-to path     : Specify $(echolor $bold $red ABSOLUTE PATH) to Output Directory;" \
            "             : If not specified, it is assumed to be the Current Working Directory" \
            "   table ... : $(echolor $bold $red [REQUIRED]) Specify Name of Tables to Export; (multiple tables are accepted)" \
            "             : Following tables are available (case insensitive)" \
            "$(preecholor $bold $yellow)" \
            "                 FILE"               \
            "                 FRAGMENT"           \
            "                 FRAGMENT2FRAGMENT"  \
            "                 GLOBAL_SETTING"     \
            "                 TAG"                \
            "                 TAG2FRAGMENT"       \
            "                 TAG2TAG" \
            "$(postecholor)" \
            "" \
            "    EXAMPLE  : ${hostScript} -from ${defaultInput} -to ${curDir} fragment tag tag2fragemnt" \
            "";
}

[ ! "$1" ] && help

### Array to contain the name of tables to export
unset tables

### Collect user parameters
debug=
while [ "$1" ]; do
    case "$1" in
        -from) inputPath=$2; shift ;;
        -to) outputDir=$2; shift ;;
        -debug) debug=debug ;;
        -help | -h | -\?) help ;;
        -*) onUnknownOption $1 ;;
        *) tables+=($1) ;;
    esac
    shift
done

### Set up Java class path to H2 Shell
setupClasspath "$debug"

### Table names are minimum required parameters
if [ ! "${tables}" ]; then
    printf "[ %s ][ %s ] Table names are required!\n[ %s ] For help, run %s\n" \
        "${hostScript}" "$(echolor $bold $magenta ERROR)" \
        "${hostScript}" "$(echolor $reverse $cyan "${hostScript}" -?)"
    exit 1
fi

### Path to the database
inputPath="${inputPath:-"$HOME/.civilizer/database/civilizer"}"

### Add '.h2.db' prefix if it is missing
[ '.h2.db' != "${inputPath:(-6)}" ] && inputPath+=.h2.db

### Confirm that the database file exists; If not, abort the operation
if [ ! -f "${inputPath}" ]; then
    printf "[ %s ][ %s ] The database file '%s' doesn't exist!\n[ %s ] For help, run %s\n" \
        "${hostScript}" "$(echolor $bold $magenta ERROR)" \
        "${inputPath}" \
        "${hostScript}" "$(echolor $reverse $cyan "${hostScript}" -?)" && \
    exit 1
fi

### Strip the prefix of the database file (Input requirement by H2)
inputPath="${inputPath%.h2.db}"

### Output path will be the current working directory, if not specified by the user
outputDir=${outputDir:-"$curDir"}

### Export the content of each database table to a .CSV file one by one
for t in ${tables[@]}; do
    outputCsv="${outputDir}/${t}-`date \"+%y-%m-%d_%Hh%Mm%Ss\"`.csv"
    [[ $debug ]] && echolor $bold $green outputDir '=>' ${outputCsv} && echo

    command="call csvwrite ( '${outputCsv}', 'select * from ${t}' )"
    [[ $debug ]] && echolor $bold $green command '=>'  ${command} && echo

    ### Export data using H2 Shell
    ### For more details, visit http://www.h2database.com/javadoc/org/h2/tools/Shell.html
    ${debug:+echo} java -cp "$classPath" "org.h2.tools.Shell" -url "jdbc:h2:${inputPath}" -user sa -sql "${command}"

    [ -f "${outputCsv}" ] && printf "[ ${hostScript} ] Table '${t}' has been exported to $(echolor $bold $cyan ${outputCsv})\n\n"
done

