#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }
hash heroku 2>/dev/null || { echo >&2 "Deployment requires Heroku-CLI but it's not installed.  Aborting."; exit 1; }

PS3="What version to deploy?"$'\n'
options=("Minimal" "Free" "Quit")
version="minimal"
select opt in "${options[@]}"
do
    case $opt in
        "Minimal")
            version="minimal"
            break
            ;;
        "Free")
            version="free"
            break
            ;;
        "Quit")
            exit 0
            ;;
        *) echo invalid option;;
    esac
done

echo "deploiying" $version

sbt $version/docker
cd $version/target/docker
heroku container:push web --app desolate-shore-33312