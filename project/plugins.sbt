resolvers ++= Seq(
  Classpaths.typesafeReleases,
  Classpaths.sbtPluginReleases,
  Resolver.sonatypeRepo("snapshots")
)

logLevel := Level.Info

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")