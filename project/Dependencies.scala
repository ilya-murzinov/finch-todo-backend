import sbt._

object Dependencies {
  lazy val finchVersion = "0.31.0"
  lazy val circeVersion = "0.12.3"

  lazy val finch = "com.github.finagle" %% "finchx-core" % finchVersion
  lazy val finchCirce = "com.github.finagle" %% "finchx-circe" % finchVersion
  lazy val circe = "io.circe" %% "circe-generic" % circeVersion
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.3" % Test
  lazy val scalatestScalacheck = "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test
  lazy val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.2" % Test

  val all = Seq(finch, finchCirce, circe, scalatest, scalatestScalacheck, scalacheck)
}
