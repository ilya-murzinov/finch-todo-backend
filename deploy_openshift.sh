#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }

sbt -mem 256 universal:packageZipTarball

git add -f target/universal/finch-todo-backend-0.1.0.tgz
git commit -am 'temp'
git push -f openshift $(git rev-parse --abbrev-ref HEAD):master
git reset --hard HEAD~1
