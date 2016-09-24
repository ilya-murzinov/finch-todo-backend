FROM frolvlad/alpine-oraclejdk8

RUN mkdir -p /opt/project
ADD target/finch-todo-backend-assembly-1.0.jar /opt/project

WORKDIR /opt/project

CMD java -Dhttp.port=$PORT -Dhttp.externalUrl=https://desolate-shore-33312.herokuapp.com -jar finch-todo-backend-assembly-1.0.jar