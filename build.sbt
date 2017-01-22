import sbtdocker.Instructions._
import sbtdocker._

lazy val finchVersion = "0.12.0"
lazy val catsVersion = "0.9.0"
lazy val circeVersion = "0.7.0"
lazy val configVersion = "1.3.1"

val allSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.12.1",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "com.github.finagle" %% "finch-core" % finchVersion,
    "com.github.finagle" %% "finch-circe" % finchVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "com.typesafe" % "config" % configVersion,
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  ),
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
)

lazy val finchTodoBackend = project
  .in(file("."))
  .settings(name := "finch-todo-backend")
  .settings(allSettings)
  .aggregate(minimal, free)
  .dependsOn(core)

lazy val core = project
  .settings(moduleName := "core")
  .settings(allSettings)

lazy val minimal = project
  .settings(name := "finch-todo-backend-minimal")
  .settings(moduleName := "minimal")
  .settings(allSettings)
  .settings(mainClass := Some("todo.backend.minimal.Main"), dockerSettings)
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .dependsOn(core)

lazy val free = project
  .settings(moduleName := "free")
  .settings(name := "finch-todo-backend-free")
  .settings(allSettings)
  .settings(
    libraryDependencies ++= Seq("org.typelevel" %% "cats-free" % catsVersion),
    mainClass in (Compile, run) := Some("todo.backend.free.Main"),
    dockerSettings
  )
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .dependsOn(core)

val dockerSettings = dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("java")
    copy(appDir, targetDir)
    cmd(
      "sh",
      "-c",
      s"$targetDir/bin/${executableScriptName.value} -Dhttp.port=$$PORT -Dhttp.externalUrl='https://desolate-shore-33312.herokuapp.com'"
    )
  }
}

val validateCommands = List(
  "clean",
  "free/compile",
  "free/test:compile",
  "free/test",
  "minimal/compile",
  "minimal/test:compile",
  "minimal/test"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))
