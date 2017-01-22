#!/bin/bash

hash sbt 2>/dev/null || { echo >&2 "Deployment requires SBT but it's not installed.  Aborting."; exit 1; }
hash heroku 2>/dev/null || { echo >&2 "Deployment requires Heroku-CLI but it's not installed.  Aborting."; exit 1; }

sbt -mem 256 docker
cd target/docker
heroku container:push web --app desolate-shore-33312