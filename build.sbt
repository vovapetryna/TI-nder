name := "MyTinder"
version := "0.1"
scalaVersion := "2.13.4"

val akkaVersion       = "2.6.13"
val akkaHttpVersion   = "10.2.4"
val argonautVersion   = "6.2.5"
val argonautShapeless = "1.2.0"

lazy val app = (project in file("app"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz"                 %% "scalaz-core"            % "7.2.27",
      "com.typesafe.akka"          %% "akka-actor-typed"       % akkaVersion,
      "com.typesafe.akka"          %% "akka-http"              % akkaHttpVersion,
      "com.typesafe.akka"          %% "akka-stream"            % akkaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"          % "3.9.2",
      "ch.qos.logback"             % "logback-classic"         % "1.2.3",
      "io.argonaut"                %% "argonaut"               % argonautVersion,
      "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % argonautShapeless,
      "com.github.alexarchambault" %% "argonaut-refined_6.2"   % argonautShapeless
    )
  )
  .settings(unmanagedResourceDirectories in Compile += (sourceDirectory.value / "../../js"))
  .settings(
    name := "MyTinder",
    version := "0.1",
    scalaVersion := "2.13.4"
  )

lazy val root = (project in file("."))
  .settings(
    name := "MyTinder",
    version := "0.1",
    scalaVersion := "2.13.4"
  )
  .aggregate(app)

addCommandAlias("root", ";project app; run")
