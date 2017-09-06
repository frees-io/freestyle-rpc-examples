lazy val root = project
  .in(file("."))
  .settings(name := "freestyle-rpc-examples")
  .settings(moduleName := "root")
  .aggregate(`demo-greeting`, `demo-protocolgen`, `demo-routeguide`)

lazy val `third-party` = project
  .in(file("third_party"))
  .settings(thirdPartySettings: _*)

lazy val `demo-greeting` = project
  .in(file("greeting"))
  .settings(moduleName := "demo-greeting")
  .settings(scalaPBSettings: _*)

lazy val `demo-http` = project
  .in(file("http"))
  .aggregate(`third-party`, `demo-greeting`)
  .dependsOn(`third-party`, `demo-greeting`)
  .settings(moduleName := "demo-http")
  .settings(scalaPBSettings: _*)
  .settings(httpDemoSettings: _*)

lazy val `demo-protocolgen` = project
  .in(file("protocolgen"))
  .settings(moduleName := "demo-protocolgen")
  .settings(protoGenTaskSettings: _*)

lazy val `demo-routeguide` = project
  .in(file("routeguide"))
  .settings(moduleName := "demo-routeguide")
  .settings(protoGenTaskSettings: _*)