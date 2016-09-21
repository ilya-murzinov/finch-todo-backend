FROM java:8

RUN mkdir /opt/project
ADD target/finch-todo-backend-assembly-1.0.jar /opt/project

WORKDIR /opt/project

CMD java -Dhttp.port=$PORT -jar finch-todo-backend-assembly-1.0.jar