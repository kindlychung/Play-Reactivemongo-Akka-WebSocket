name := "PlayReactivemongoAkkaWebsocket"

version := "1.0"

lazy val `playreactivemongoakkawebsocket` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

val reactiveMongoVersion = "0.13.0-play26"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.13.0-play26",
  "org.reactivemongo" %% "reactivemongo-akkastream" % "0.13.0"
)

libraryDependencies += guice

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  