lazy val V = new {
  lazy val circe          = "0.9.1"
  lazy val frees          = "0.7.0"
  lazy val freesRPC       = "0.11.0"
  lazy val log4s          = "1.4.0"
  lazy val logbackClassic = "1.2.3"
  lazy val monix          = "3.0.0-M3"
}

lazy val `demo-routeguide` = project
  .in(file("routeguide"))
  .settings(name := "frees-rpc-examples")
  .settings(moduleName := "demo-routeguide")
  .settings(Seq(
    organization := "frees-io",
    organizationName := "47 Degrees",
    scalaVersion := "2.12.4",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("beyondthelines", "maven")
    ),
    libraryDependencies ++= Seq(
      "io.frees"       %% "frees-async-cats-effect" % V.frees,
      "io.frees"       %% "frees-rpc-config"        % V.freesRPC,
      "io.frees"       %% "frees-rpc-client-netty"  % V.freesRPC,
      "io.frees"       %% "frees-core"              % V.frees,
      "io.frees"       %% "frees-rpc-server"        % V.freesRPC,
      "io.frees"       %% "frees-monix"             % V.frees,
      "org.log4s"      %% "log4s"                   % V.log4s,
      "ch.qos.logback" % "logback-classic"          % V.logbackClassic
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % V.circe)
  ): _*)
  .settings(
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-io",
      baseDirectory.value / "src" / "main" / "scala-task"
    )
  )
  .settings(
    Seq(
      addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
      libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
      scalacOptions += "-Xplugin-require:macroparadise",
      scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
    ): _*
  )

addCommandAlias("runServer", ";project demo-routeguide;runMain routeguide.ServerApp")
addCommandAlias("runClientIO", ";project demo-routeguide;runMain routeguide.ClientAppIO")
addCommandAlias("runClientTask", ";project demo-routeguide;runMain routeguide.ClientAppTask")
