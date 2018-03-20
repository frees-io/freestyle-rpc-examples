lazy val V = new {
  lazy val circe    = "0.9.2"
  lazy val frees    = "0.8.0"
  lazy val freesRPC = "0.12.0"
  lazy val log4s    = "1.6.0"
  lazy val logback  = "1.2.3"
  lazy val paradise = "3.0.0-M11"
}

lazy val `demo-routeguide` = project.in(file("routeguide")).settings(
  name := "frees-rpc-examples",
  moduleName := "demo-routeguide",
  organization := "frees-io",
  organizationName := "47 Degrees",
  scalaVersion := "2.12.4",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("beyondthelines", "maven")
  ),
  libraryDependencies ++= Seq(
    "io.frees"       %% "frees-rpc-config"       % V.freesRPC,
    "io.frees"       %% "frees-rpc-client-netty" % V.freesRPC,
    "io.frees"       %% "frees-monix"            % V.frees,
    "io.circe"       %% "circe-generic"          % V.circe,
    "io.circe"       %% "circe-parser"           % V.circe,
    "org.log4s"      %% "log4s"                  % V.log4s,
    "ch.qos.logback"  % "logback-classic"        % V.logback
  ),
  Compile / unmanagedSourceDirectories ++= Seq(
    baseDirectory.value / "src" / "main" / "scala-io",
    baseDirectory.value / "src" / "main" / "scala-task"
  ),
  addCompilerPlugin("org.scalameta" % "paradise" % V.paradise cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-Ywarn-unused-import",
    "-Xplugin-require:macroparadise"
  ),
  Compile / console / scalacOptions --= Seq(
    "-Ywarn-unused-import",
    "-Xplugin-require:macroparadise" // macroparadise plugin doesn't work in repl yet.
  )
)

addCommandAlias("runServer"    , ";project demo-routeguide;runMain routeguide.ServerApp")
addCommandAlias("runClientIO"  , ";project demo-routeguide;runMain routeguide.ClientAppIO")
addCommandAlias("runClientTask", ";project demo-routeguide;runMain routeguide.ClientAppTask")
