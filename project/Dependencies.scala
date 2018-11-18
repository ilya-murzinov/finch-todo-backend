import sbt._

object Dependencies {
  lazy val finchVersion = "0.26.0"
  lazy val circeVersion = "0.9.3"

  lazy val finch = "com.github.finagle" %% "finch-core" % finchVersion
  lazy val finchCirce = "com.github.finagle" %% "finch-circe" % finchVersion
  lazy val circe = "io.circe" %% "circe-generic" % circeVersion
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

  val all = Seq(finch, finchCirce, circe, scalatest, scalacheck)
}
