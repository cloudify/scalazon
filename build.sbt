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
  "com.amazonaws"       % "aws-java-sdk"    % "1.7.13",
  "org.scalatest"      %% "scalatest"       % "2.0"     % "test",
  "org.mockito"         % "mockito-all"     % "1.9.0"   % "test"
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

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("scala", "aws", "amazon", "client", "kinesis")
