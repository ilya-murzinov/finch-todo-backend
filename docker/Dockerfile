FROM anapsix/alpine-java:8

ADD finch-todo-backend.jar ./
ADD run.sh ./
RUN chmod +x run.sh

CMD ./run.sh