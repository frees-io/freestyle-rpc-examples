lazy val root = project
  .in(file("."))
  .settings(name := "freestyle-rpc-examples")
  .settings(moduleName := "root")
  .dependsOn(`demo-routeguide`)
  .aggregate(`demo-routeguide`)

lazy val `demo-routeguide` = project
  .in(file("routeguide"))
  .settings(moduleName := "demo-routeguide")
  .settings(protoGenTaskSettings: _*)
  .settings(libraryDependencies ++= circeDeps)
  .settings(
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-future",
      baseDirectory.value / "src" / "main" / "scala-task"
    )
  )