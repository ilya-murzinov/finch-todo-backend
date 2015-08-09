name := "finch-todo-backend"

version := "1.0"

scalaVersion := "2.11.7"
lazy val finchVersion = "0.10.0"
lazy val circeVersion = "0.3.0"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion
)

enablePlugins(JavaAppPackaging)
