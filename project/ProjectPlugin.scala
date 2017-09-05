import ProjectPlugin.autoImport.V
import sbt.Keys._
import sbt._
import sbtprotoc.ProtocPlugin.autoImport.PB
import com.trueaccord.scalapb.compiler.{Version => cv}

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    object V {
      lazy val frees    = "0.3.1"
      lazy val freesRPC = "0.0.1-SNAPSHOT"
    }

    lazy val GOPATH = Option(sys.props("go.path")).getOrElse("/your/go/path")

  }

  lazy val commandAliases: Seq[Def.Setting[_]] =
    addCommandAlias(
      "runServer",
      ";project demo-greeting;runMain freestyle.rpc.demo.greeting.GreetingServerApp") ++
      addCommandAlias(
        "runClient",
        ";project demo-greeting;runMain freestyle.rpc.demo.greeting.GreetingClientApp") ++
      addCommandAlias("validateHttpDemo", ";project demo-http;clean;compile;test") ++
      addCommandAlias(
        "runS",
        ";project demo-protocolgen;runMain freestyle.rpc.demo.protocolgen.ServerApp") ++
      addCommandAlias(
        "runC",
        ";project demo-protocolgen;runMain freestyle.rpc.demo.protocolgen.ClientApp")

  lazy val demoCommonSettings = Seq(
    name := "freestyle-rpc-examples",
    organization := "frees-io",
    organizationName := "47 Degrees",
    scalaVersion := "2.12.2",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("beyondthelines", "maven")),
    PB.protoSources.in(Compile) := Seq(sourceDirectory.in(Compile).value / "proto"),
    PB.targets.in(Compile) := Seq(scalapb.gen() -> sourceManaged.in(Compile).value),
    libraryDependencies ++= Seq(
      "io.frees"               %% "freestyle"            % V.frees,
      "io.frees"               %% "freestyle-rpc"        % V.freesRPC,
      "io.grpc"                % "grpc-netty"            % cv.grpcJavaVersion,
      "com.trueaccord.scalapb" %% "scalapb-runtime"      % cv.scalapbVersion % "protobuf",
      "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % cv.scalapbVersion
    )
  )

  lazy val scalaMetaSettings = Seq(
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full),
    libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
    scalacOptions += "-Xplugin-require:macroparadise"
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    scalaMetaSettings ++ demoCommonSettings ++ commandAliases

}
