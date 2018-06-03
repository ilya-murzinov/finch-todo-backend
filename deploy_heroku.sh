#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }
hash heroku 2>/dev/null || { echo >&2 "Deployment requires Heroku-CLI but it's not installed.  Aborting."; exit 1; }

sbt clean assembly
cd docker
cp ../target/scala-2.12/finch-todo-backend.jar ./
docker build -t finch-todo-backend ./
heroku container:push web --app desolate-shore-33312
heroku container:release web --app desolate-shore-33312
rm finch-todo-backend.jar