scalaVersion := "3.3.8"
organization := "com.lukewassink"

val toolkitV = "0.9.2"
val toolkit = "org.scala-lang" %% "toolkit" % toolkitV
val toolkitTest = "org.scala-lang" %% "toolkit-test" % toolkitV

lazy val root = rootProject
  .autoAggregate
  .settings(
    publish / skip := true
  )

lazy val simulation = (project in file("simulation"))
  .settings(
    libraryDependencies ++= Seq(
      toolkit,
      toolkitTest % Test,
      "org.scalactic" %% "scalactic" % "3.2.20",
      "org.scalatest" %% "scalatest" % "3.2.20" % "test"
    )
  )