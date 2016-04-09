name := "finch-todo-backend"

version := "1.0"

scalaVersion := "2.11.7"
lazy val finchVersion = "0.10.0"
lazy val circeVersion = "0.3.0"
lazy val reactiveMongoVersion = "0.11.11"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.twitter" %% "bijection-util" % "0.8.1"
)

target := file("/tmp/sbt") / name.value