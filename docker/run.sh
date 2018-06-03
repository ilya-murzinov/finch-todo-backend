#!/bin/sh

java \
  -Dhttp.host=0.0.0.0 \
  -Dhttp.port=$PORT \
  -Dhttp.externalUrl='https://desolate-shore-33312.herokuapp.com' \
  -jar finch-todo-backend.jar