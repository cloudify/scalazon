// sbt-dependency-graph settings
net.virtualvoid.sbt.graph.Plugin.graphSettings

// sbt-release settings
releaseSettings

organization := "io.github.cloudify"

name := "scalazon"

description := "Opinionated, idiomatic Scala library for Amazon Web Services."

homepage := Some(url("https://github.com/cloudify/scalazon"))

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.2")

// release cross builds
ReleaseKeys.crossBuild := true

licenses += ( "MIT" -> url("http://opensource.org/licenses/MIT") )

unmanagedSourceDirectories in Compile += baseDirectory.value / "examples"

libraryDependencies ++= Seq(
  "com.amazonaws"       % "aws-java-sdk"    % "1.9.8",
  "org.mockito"         % "mockito-all"     % "1.10.8"   % "test"
)

def scalatestDependency(scalaVersion: String) = scalaVersion match {
  case v if v.startsWith("2.9") =>  "org.scalatest" %% "scalatest"  % "1.9.2" % "test"
  case _ =>                         "org.scalatest" %% "scalatest"  % "2.2.2" % "test"
}

// use different versions of scalatest for different versions of scala
libraryDependencies <+= scalaVersion(scalatestDependency(_))

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
