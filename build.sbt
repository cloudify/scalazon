net.virtualvoid.sbt.graph.Plugin.graphSettings

name := "scalazon"

scalaVersion := "2.10.3"

unmanagedSourceDirectories in Compile += baseDirectory.value / "examples"

libraryDependencies ++= Seq(
    "org.apache.httpcomponents" % "httpclient" % "4.2.3",
    "org.freemarker" % "freemarker" % "2.3.18",
    "org.springframework" % "spring-context" % "3.0.7.RELEASE",
    "javax.mail" % "mail" % "1.4.3",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.1"
)