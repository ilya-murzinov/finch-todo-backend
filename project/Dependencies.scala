import sbt._

object Dependencies {
  lazy val finchVersion = "0.28.0"
  lazy val circeVersion = "0.11.1"

  lazy val finch = "com.github.finagle" %% "finchx-core" % finchVersion
  lazy val finchCirce = "com.github.finagle" %% "finchx-circe" % finchVersion
  lazy val circe = "io.circe" %% "circe-generic" % circeVersion
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.7" % Test
  lazy val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

  val all = Seq(finch, finchCirce, circe, scalatest, scalacheck)
}
