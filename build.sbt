name := "finch-todo-backend"

version := "1.0"

scalaVersion := "2.11.7"
lazy val finchVersion = "0.11.0-M3"
lazy val catsVersion = "0.7.2"
lazy val circeVersion = "0.5.1"
lazy val reactiveMongoVersion = "0.11.11"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.twitter" %% "bijection-util" % "0.8.1",
  "com.typesafe" % "config" % "1.2.1"
)

mainClass in (Compile, run) := Some(
  "com.github.ilyamurzinov.todo.backend.Main")

val validateCommands = List(
  "clean",
  "scalafmtTest",
  "test:scalafmtTest",
  "compile",
  "test:compile",
  "test"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))

target := file("/tmp/sbt") / name.value
