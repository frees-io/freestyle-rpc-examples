lazy val V = new {
  lazy val frees    = "0.4.0"
  lazy val freesRPC = "0.0.8"
  lazy val circe    = "0.9.0-M1"
  lazy val monix    = "3.0.0-M1"
}

lazy val `demo-routeguide` = project
  .in(file("routeguide"))
  .settings(name := "frees-rpc-examples")
  .settings(moduleName := "demo-routeguide")
  .settings(Seq(
    organization := "frees-io",
    organizationName := "47 Degrees",
    scalaVersion := "2.12.3",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("beyondthelines", "maven")
    ),
    libraryDependencies ++= Seq(
      "io.frees" %% "frees-core"              % V.frees,
      "io.frees" %% "frees-async"             % V.frees,
      "io.frees" %% "frees-async-cats-effect" % V.frees,
      "io.frees" %% "frees-config"            % V.frees,
      "io.frees" %% "frees-logging"           % V.frees,
      "io.frees" %% "frees-rpc"               % V.freesRPC,
      "io.monix" %% "monix"                   % V.monix
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % V.circe)
  ): _*)
  .settings(
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-future",
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
addCommandAlias("runClientF", ";project demo-routeguide;runMain routeguide.ClientAppF")
addCommandAlias("runClientT", ";project demo-routeguide;runMain routeguide.ClientAppT")
