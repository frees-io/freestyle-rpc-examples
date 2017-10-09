import sbt.Keys._
import sbt._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    object V {
      lazy val frees    = "0.3.1"
      lazy val freesRPC = "0.0.6"
      lazy val circe    = "0.8.0"
      lazy val monix    = "2.3.0"
    }

    val circeDeps: Seq[ModuleID] = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % V.circe)

  }

  import autoImport._

  lazy val commandAliases: Seq[Def.Setting[_]] =
    addCommandAlias("runServer", ";project demo-routeguide;runMain routeguide.ServerApp") ++
      addCommandAlias("runClientF", ";project demo-routeguide;runMain routeguide.ClientAppF") ++
      addCommandAlias("runClientT", ";project demo-routeguide;runMain routeguide.ClientAppT")

  lazy val demoCommonSettings = Seq(
    organization := "frees-io",
    organizationName := "47 Degrees",
    scalaVersion := "2.12.3",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("beyondthelines", "maven")
    ),
    libraryDependencies ++= circeDeps ++ Seq(
      "io.frees" %% "freestyle"             % V.frees,
      "io.frees" %% "freestyle-async"       % V.frees,
      "io.frees" %% "freestyle-config"      % V.frees,
      "io.frees" %% "freestyle-logging"     % V.frees,
      "io.frees" %% "freestyle-async-monix" % V.frees,
      "io.frees" %% "frees-rpc"             % V.freesRPC,
      "io.monix" %% "monix-cats"            % V.monix
    )
  )

  lazy val scalaMetaSettings = Seq(
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
    libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
    scalacOptions += "-Xplugin-require:macroparadise",
    scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    commandAliases ++ demoCommonSettings ++ scalaMetaSettings

}
