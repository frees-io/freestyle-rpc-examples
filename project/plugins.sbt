resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("beyondthelines", "maven")
)
addSbtPlugin("io.frees" % "sbt-frees-rpc-idlgen" % "0.12.0")
