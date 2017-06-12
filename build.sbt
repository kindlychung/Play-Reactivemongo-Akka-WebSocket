name := "PlayReactivemongoAkkaWebsocket"

version := "1.0"

lazy val `playreactivemongoakkawebsocket` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val reactiveMongoVersion = "0.12.3"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "reactivemongo-akkastream" % reactiveMongoVersion
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  