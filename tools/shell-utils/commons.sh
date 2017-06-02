#!/usr/bin/env bash

### Font colors
red=1 green=2 yellow=3 blue=4 magenta=5 cyan=6

### Font attributes
bold=1 underline=4 reverse=7

preecholor() {
    printf '\e[%s;3%dm' "$1" "$2"
}

postecholor() {
    printf '%s\e[0m' "$*"
}

### Colorful echo; echo + color
### Usage: echolor [attr] [color] [arbitrary messages...]
### e.g. : echolor $underline $magenta Current dir is $PWD
echolor() {
    preecholor "$1" "$2"
    shift 2
    postecholor "$*"
}

### Print script variables (for debug purpose)
### Usage: printvar [var (without $)]
### e.g. : printvar PWD # will print content of $PWD
printvar() {
    printf '[ %s ][ %s ] $%s = %s\n' "$hostScript" $(echolor "$bold" "$red" DEBUG) $(echolor "$bold" "$cyan" "$1") "${!1}"
}

### Helper function to print common part of usage message
usage() {
    printf "[ %s ] Options\n" "$hostScript"
    printf "\t%s\n" "$@"
    printf "\t-help, -h, -? : Show this message\n"
    exit 0
}

### Helper function to detect an unknown command option;
### It may help users realize their typos or other mistakes...
onUnknownOption() {
    local ans
    read -n 1 -p "[ $hostScript ] : You've specified an unknown option $(echolor $reverse $cyan "$1"). Ignore it and proceed? (y or n) :" ans
    echo
    [ "$ans" == "n" ] && exit 1
}

### Check if the given path exists;
checkPath() {
    [ -e "$1" ] || { printf "[ %s ][ %s ] Can't access the path %s\n" "$hostScript" $(echolor $bold $magenta ERROR) $(echolor $reverse $cyan "$1") && exit 1; }
}

### Build up Java classpath necessary for Civilizer to run;
setupClasspath() {
    ## First assume the script is running in production package;
    webappPath="civilizer"
    extraPath="extra"
    extraLibPath="$extraPath/lib"

    if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f "$extraLibPath/jetty-runner.jar" ]; then
        ## If the script runs in source package, we may get here;
        webappPath="../../target/civilizer-1.0.0.CI-SNAPSHOT"
        extraPath="../../target/extra"
        extraLibPath="../../extra/lib"

        if [ ! -f "$webappPath/WEB-INF/web.xml" -o ! -f "$extraLibPath/jetty-runner.jar" ]; then
            ## Where are you running the script?
            printf "[ %s ][ %s ] Civilizer can't be found!\n" "$hostScript" $(echolor $bold $magenta ERROR)
            exit 1
        fi
    fi

    ## Get absolute path; inefficient though...
    webappPath=$( cd "$webappPath" && pwd )
    extraPath=$( cd "$extraPath" && pwd )
    extraLibPath=$( cd "$extraLibPath" && pwd )

    ## We got the classpath here;
    classPath="$webappPath/WEB-INF/classes:$extraLibPath/*:$webappPath/WEB-INF/lib/*:$extraPath"

    ## Ensure each class path exists
    local IFS=:*
    for cp in $classPath; do
        [ $cp ] && checkPath $cp
    done

    [ "$1" = "debug" ] && {
        printvar webappPath
        printvar extraPath
        printvar extraLibPath
        printvar classPath
        echo
    }

    ## On Cygwin, everything is NOT compatible to genuine *nix platforms.
    ## For example, the following java command won't work on Cygwin.
    ##     java -cp "a.jar:b.jar" hello.class
    ## Because Cygwin uses Windows's java runtime, and Windows java requires semi-colon separated classpath parameter.
    ## Another problem is that Cygwin has file path convention of something like "/cygdrive/c/Windows" (equivalent to 'C:\Windows') and of course, Windows java doesn't recognize that convention.
    ##
    ## Solution : Use cygpath provided by Cygwin:
    ##     javac -cp "$(cygpath -pw "$CLASSPATH")" hello.java)"
    local uname=$( uname )
    [ "${uname:0:6}" = "CYGWIN" ] &&  classPath=$( cygpath -pw "$classPath" )
}

versionAllowed() {
    ### $1 is the minimum required version, $2 is the version in question;
    ### If $1 < $2, return 0 (the input version is allowed)
    ### otherwise, return 1 (not allowed)
    local earlierVersion=$( printf "%s\n%s" "$1" "$2" | tr -cs '0-9\n' '.' | sort -t . -k1,1 -k2,2 -k3,3 -k4,4 -k5,5 -k6,6 -k7,7 -k8,8 -k9,9 -g | head -1 )

    [ "$earlierVersion" = "$1" ]
}

confirmJre() {
    type java > /dev/null 2>&1 || {
        printf "[ %s ][ %s ] Can't find JRE (Java Runtime Environment)!\n" "$hostScript" "$(echolor $bold $magenta ERROR)"
        printf "\t( Downlaod and install JRE from Oracle )\n"
        exit 1
    }

    local minimumVesion=${1:-1.7}
    local installedVersion=$( java -version 2>&1 | grep --color=never "java version " | cut -d \" -f 2 )
    versionAllowed "$minimumVesion" "$installedVersion" || {
        printf "[ %s ][ %s ] The installed JRE version is too old to run Civilizer\n" "$hostScript" "$(echolor $bold $magenta ERROR)"
        printf "\t( You need at least JRE version of $minimumVesion )\n"
        exit 1
    }
}

