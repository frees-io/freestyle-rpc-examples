lazy val `demo-routeguide` = project
  .in(file("routeguide"))
  .settings(name := "freestyle-rpc-examples")
  .settings(moduleName := "demo-routeguide")
  .settings(
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-future",
      baseDirectory.value / "src" / "main" / "scala-task"
    )
  )
