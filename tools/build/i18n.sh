#!/bin/bash

confirmPrerequisite() {
    type native2ascii > /dev/null 2>&1 || {
        printf "[ %s ][ %s ] Can't find native2ascii!\n" "$hostScript" "$(echolor $bold $magenta ERROR)"
        printf "\t( You need to install JDK to use this script! )\n"
        exit 1
    }
}

hostScript=${0##*/}
scriptDir=${0%/*}

### Move to the directory where this script is located
pushd "$scriptDir" > /dev/null 2>&1

### Load the script file containing necessary utility functions
[ -f "shell-utils/commons.sh" ] && source "shell-utils/commons.sh" || {
    [ -f "../shell-utils/commons.sh" ] && source "../shell-utils/commons.sh";
} || {
    echo "[ $hostScript ][ ERROR ] Can't find shell-utils/common.sh!"
    echo -e "\t ( You may be running the script from a wrong place... )"
    exit 1
}

### Confirm required tools are installed
confirmPrerequisite

### Collect user parameters
localeName=ja
toPath=
fromPath=
hr=
debug=
while true; do
    case "$1" in
        -l) localeName=$2; shift ;;
        -to) toPath=$2; shift ;;
        -from) fromPath=$2 ;;
        -hr) hr=yes ;;
        -debug) debug=debug ;;
        -help | -h | -\?) usage \
            "-l localeName : Specify the locale name (e.g. ja)" \
            "-to path      : Specify the output resource file with UTF8 encoding" \
            "-from path    : Specify the input resource file with UTF8 encoding" \
            "-hr           : Convert Help messages" \
            ;;
        -*) onUnknownOption $1 ;;
        *) break ;;
    esac
    shift
done

### Retrieve the path where i18n resource files are placed
pushd ../../src/main/resources/i18n > /dev/null 2>&1
i18nPath="${PWD}"
popd > /dev/null 2>&1
popd > /dev/null 2>&1

### The resource file; It is encoded with ISO-8859-1
rscPath="${i18nPath}/MessageResources_${localeName}.properties"
[ "${hr}" == "yes" ] && rscPath="${i18nPath}/HelpResources_${localeName}.properties"

if [ -f "${fromPath}" ]; then
    ${debug:+echo} native2ascii -encoding utf8 "${fromPath}" "${rscPath}"
elif [ "${toPath}" ]; then
    ${debug:+echo} native2ascii -encoding utf8 -reverse "${rscPath}" "${toPath}"
else
    echo "[ $hostScript ][$(echolor $bold $red ERROR )] Either of [-to path] or [-from path] is REQUIRED!"
    exit 1
fi

