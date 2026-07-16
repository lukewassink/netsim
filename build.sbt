scalaVersion := "3.8.4"
organization := "com.lukewassink"
ThisBuild / version := "0.0.0-SNAPSHOT"

val toolkitV = "0.9.2"
val toolkit = "org.scala-lang" %% "toolkit" % toolkitV
val toolkitTest = "org.scala-lang" %% "toolkit-test" % toolkitV

// Silence errors when I run the unit tests.
javaOptions += "--sun-misc-unsafe-memory-access=allow"

val sharedDependencies = Seq(
  toolkit,
  toolkitTest % Test,
  "org.scalactic" %% "scalactic" % "3.2.20",
  "org.scalatest" %% "scalatest" % "3.2.20" % "test"
)

lazy val root = rootProject.autoAggregate
  .settings(
    publish / skip := true
  )

lazy val simulation = (project in file("simulation")).settings(
  libraryDependencies ++= sharedDependencies
)

lazy val runner = (project in file("runner"))
  .settings(
    libraryDependencies ++= sharedDependencies
  )
  .dependsOn(simulation % "compile->compile;test->test")
