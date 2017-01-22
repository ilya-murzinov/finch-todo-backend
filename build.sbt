import sbtdocker.Instructions._
import sbtdocker._

name := "finch-todo-backend"

version := "0.1.0"

scalaVersion := "2.12.1"

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

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
  "-Xfatal-warnings"
)

lazy val finchVersion = "0.12.0"
lazy val catsVersion = "0.9.0"
lazy val circeVersion = "0.7.0"
lazy val configVersion = "1.3.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "com.typesafe" % "config" % configVersion,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)

mainClass in (Compile, run) := Some(
  "com.github.ilyamurzinov.todo.backend.Main"
)

val validateCommands = List(
  "clean",
  "compile",
  "test:compile",
  "test"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("java")
    copy(appDir, targetDir)
    cmd(
      "sh",
      "-c",
      s"$targetDir/bin/${executableScriptName.value} -Dhttp.port=$$PORT"
    )
  }
}
