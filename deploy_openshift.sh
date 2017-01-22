#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }

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

sbt $version/universal:packageZipTarball

git add -f $version/target/universal/finch-todo-backend-free-0.1.0.tgz
git commit -am 'temp'
git push -f openshift $(git rev-parse --abbrev-ref HEAD):master
git reset --hard HEAD~1
