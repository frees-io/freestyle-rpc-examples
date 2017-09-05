import com.trueaccord.scalapb.compiler.{Version => cv}

lazy val root = project
  .in(file("."))
  .settings(name := "freestyle-rpc-examples")
  .settings(moduleName := "root")
  .aggregate(`demo-greeting`, `demo-protocolgen`)

lazy val protogen = taskKey[Unit]("Generates .proto files from freestyle-rpc service definitions")

lazy val `demo-protocolgen` = project
  .in(file("protocolgen"))
  .settings(moduleName := "demo-protocolgen")
  .settings(
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

lazy val `demo-greeting` = project
  .in(file("greeting"))
  .settings(moduleName := "demo-greeting")
  .settings(
    Seq(
      libraryDependencies ++= Seq(
        "io.frees" %% "freestyle-async" % V.frees,
        "io.frees" %% "freestyle-config" % V.frees
      )
    ): _*)

lazy val googleApi = project
  .in(file("third_party"))
  .settings(
    PB.protoSources.in(Compile) ++= Seq(
      file(s"$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis/")
    ),
    PB.targets.in(Compile) := Seq(scalapb.gen() -> sourceManaged.in(Compile).value),
    libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % cv.scalapbVersion % "protobuf"
  )

lazy val `demo-http` = project
  .in(file("http"))
  .settings(moduleName := "demo-http")
  .aggregate(googleApi, `demo-greeting`)
  .dependsOn(googleApi, `demo-greeting`)
  .settings(
    Seq(
      PB.protocOptions.in(Compile) ++= Seq(
        "-I/usr/local/include -I.",
        s"-I$GOPATH/src",
        s"-I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis",
        "--go_out=plugins=grpc:./http/gateway",
        "--grpc-gateway_out=logtostderr=true:./http/gateway",
        "--swagger_out=logtostderr=true:./http/gateway"
      )
    ): _*
  )
