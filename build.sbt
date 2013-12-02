// sbt-dependency-graph settings
net.virtualvoid.sbt.graph.Plugin.graphSettings

// sbt-release settings
releaseSettings

organization := "io.github.cloudify"

name := "scalazon"

description := "Opinionated, idiomatic Scala library for Amazon Web Services."

homepage := Some(url("https://github.com/cloudify/scalazon"))

scalaVersion := "2.10.3"

licenses += ( "MIT" -> url("http://opensource.org/licenses/MIT") )

unmanagedSourceDirectories in Compile += baseDirectory.value / "examples"

libraryDependencies ++= Seq(
    "org.apache.httpcomponents" % "httpclient" % "4.2.3",
    "org.freemarker" % "freemarker" % "2.3.18",
    "org.springframework" % "spring-context" % "3.0.7.RELEASE",
    "javax.mail" % "mail" % "1.4.3",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.1"
)

scalacOptions ++= List(
  "-encoding", "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-target:jvm-1.6",
  "-language:_"
)

// bintray resolvers
seq(bintrayResolverSettings:_*)

resolvers += bintray.Opts.resolver.repo("cloudify", "maven")

// bintray publishing
seq(bintrayPublishSettings:_*)

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("scala", "aws", "amazon", "client")
