#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }

sbt -mem 256 clean assembly

git add -f target/scala-2.11/finch-todo-backend-assembly-1.0.jar
git commit -am 'temp'
git push -f openshift master
git reset --hard HEAD~1
rm -rf target
