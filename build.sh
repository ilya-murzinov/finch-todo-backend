mkdir target
sbt assembly
cp /tmp/sbt/finch-todo-backend/scala-2.11/finch-todo-backend-assembly-1.0.jar ./target
docker build -t finch-todo-backend .
