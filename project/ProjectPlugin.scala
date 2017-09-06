import sbt.Keys._
import sbt._
import sbtprotoc.ProtocPlugin.autoImport.PB
import com.trueaccord.scalapb.compiler.{Version => cv}

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    object V {
      lazy val frees    = "0.3.1"
      lazy val freesRPC = "0.0.1"
    }

    lazy val GOPATH: String = Option(sys.props("go.path")).getOrElse("/your/go/path")

    lazy val protogen: TaskKey[Unit] =
      taskKey[Unit]("Generates .proto files from freestyle-rpc service definitions")

    lazy val scalaPBSettings: Seq[Def.Setting[_]] = Seq(
      PB.protoSources.in(Compile) := Seq(sourceDirectory.in(Compile).value / "proto"),
      PB.targets.in(Compile) := Seq(scalapb.gen() -> sourceManaged.in(Compile).value),
      libraryDependencies ++= Seq(
        "com.trueaccord.scalapb" %% "scalapb-runtime"      % cv.scalapbVersion % "protobuf",
        "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % cv.scalapbVersion
      )
    )

    lazy val thirdPartySettings: Seq[Def.Setting[_]] = Seq(
      PB.protoSources.in(Compile) ++= Seq(
        file(s"$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis/")
      ),
      PB.targets.in(Compile) := Seq(scalapb.gen() -> sourceManaged.in(Compile).value),
      libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % cv.scalapbVersion % "protobuf"
    )

    lazy val httpDemoSettings: Seq[Def.Setting[_]] = Seq(
      PB.protocOptions.in(Compile) ++= Seq(
        "-I/usr/local/include -I.",
        s"-I$GOPATH/src",
        s"-I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis",
        "--go_out=plugins=grpc:./http/gateway",
        "--grpc-gateway_out=logtostderr=true:./http/gateway",
        "--swagger_out=logtostderr=true:./http/gateway"
      )
    )

    lazy val protoGenTaskSettings: Seq[Def.Setting[_]] = Seq(
      protogen := {
        toError(
          (runner in Compile).value
            .run(
              mainClass = "freestyle.rpc.protocol.ProtoCodeGen",
              classpath = sbt.Attributed.data((fullClasspath in Compile).value),
              options = Seq(
                (baseDirectory.value / "src" / "main" / "scala").absolutePath,
                (baseDirectory.value / "src" / "main" / "proto").absolutePath
              ),
              log = streams.value.log
            )
        )
      }
    )

  }

  import autoImport.V

  lazy val commandAliases: Seq[Def.Setting[_]] =
    addCommandAlias("validateHttpDemo", ";project demo-http;clean;compile;test") ++
      addCommandAlias(
        "runGreetingServer",
        ";project demo-greeting;runMain freestyle.rpc.demo.greeting.GreetingServerApp") ++
      addCommandAlias(
        "runGreetingClient",
        ";project demo-greeting;runMain freestyle.rpc.demo.greeting.GreetingClientApp") ++
      addCommandAlias(
        "runProtoGenServer",
        ";project demo-protocolgen;runMain freestyle.rpc.demo.protocolgen.ServerApp") ++
      addCommandAlias(
        "runProtoGenClient",
        ";project demo-protocolgen;runMain freestyle.rpc.demo.protocolgen.ClientApp")

  lazy val demoCommonSettings = Seq(
    name := "freestyle-rpc-examples",
    organization := "frees-io",
    organizationName := "47 Degrees",
    scalaVersion := "2.12.2",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("beyondthelines", "maven")
    ),
    libraryDependencies ++= Seq(
      "io.frees" %% "freestyle"        % V.frees,
      "io.frees" %% "freestyle-async"  % V.frees,
      "io.frees" %% "freestyle-config" % V.frees,
      "io.frees" %% "frees-rpc"        % V.freesRPC,
      "io.grpc"  % "grpc-netty"        % cv.grpcJavaVersion
    )
  )

  lazy val scalaMetaSettings = Seq(
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full),
    libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
    scalacOptions += "-Xplugin-require:macroparadise"
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    commandAliases ++ demoCommonSettings ++ scalaMetaSettings

}
