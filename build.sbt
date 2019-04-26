import Dependencies._

lazy val root = project.in(file("."))
  .settings(Seq(
    name := "finch-todo-backend",
    version := "0.1.0",
    scalaVersion := "2.12.8",
    libraryDependencies ++= all,
    mainClass in (Compile, run) := Some("todobackend.Main"),
    mainClass in assembly := Some("todobackend.Main"),
    assemblyJarName in assembly := "finch-todo-backend.jar",
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint",
      "-language:higherKinds",
      "-Ywarn-unused-import",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-numeric-widen",
      "-Xfatal-warnings")
  ))

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x => MergeStrategy.defaultMergeStrategy(x)
}

val validateCommands = List(
  "clean",
  "compile",
  "test:compile",
  "test"
)

addCommandAlias("validate", validateCommands.mkString(";", ";", ""))
