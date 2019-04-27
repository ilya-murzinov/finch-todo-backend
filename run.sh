#!/bin/sh

java \
  -Dhttp.host=0.0.0.0 \
  -Dhttp.port=$PORT \
  -Dhttp.externalUrl='https://finch-todo-backend.herokuapp.com' \
  -jar target/scala-2.12/finch-todo-backend.jar