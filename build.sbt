scalaVersion := "3.3.8"
organization := "com.example"

val toolkitV = "0.2.0"
val toolkit = "org.scala-lang" %% "toolkit" % toolkitV
val toolkitTest = "org.scala-lang" %% "toolkit-test" % toolkitV

libraryDependencies ++= Seq(
      toolkit,
      (toolkitTest % Test)
    )

lazy val root = rootProject
  .autoAggregate
  .settings(
    publish / skip := true
  )

lazy val simulation = (project in file("simulation"))
