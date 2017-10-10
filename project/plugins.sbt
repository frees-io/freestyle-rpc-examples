resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("beyondthelines", "maven")
)

addSbtPlugin("io.frees" % "sbt-frees-protogen" % "0.0.8")
