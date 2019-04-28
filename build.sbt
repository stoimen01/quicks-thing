name := "quicks-thing"

version := "0.1"

scalaVersion := "2.12.8"

mainClass in assembly := Some("com.quicks.Quicks")

libraryDependencies ++= {
  val akkaV = "2.5.22"
  Seq(
    "com.typesafe.akka" %% "akka-http"   % "10.1.8",
    "com.typesafe.akka" %% "akka-stream" % akkaV
  )
}