import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.8.4"
ThisBuild / organization := "com.lukewassink"
version := "0.0.0-SNAPSHOT"

// Silence errors when I run the unit tests.
javaOptions += "--sun-misc-unsafe-memory-access=allow"

val toolkitV = "0.9.2"

val sharedDependencies = Seq(
  "org.scala-lang" %% "toolkit" % toolkitV,
  "org.scala-lang" %% "toolkit-test" % toolkitV % Test,
  "org.scalactic" %% "scalactic" % "3.2.20",
  "org.scalatest" %% "scalatest" % "3.2.20" % "test"
)

lazy val root = (project in file("."))
  .aggregate(simulation.js, simulation.jvm, runner.js, runner.jvm, visualizer)
  .settings(
    publish / skip := true
  )

lazy val simulation = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("simulation"))
  .settings(
    name := "simulation",
    libraryDependencies ++= sharedDependencies
  )

lazy val runner = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("runner"))
  .settings(
    name := "runner",
    libraryDependencies ++= sharedDependencies
  )
  .dependsOn(simulation % "compile->compile;test->test")

lazy val visualizer = (project in file("visualizer"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "visualizer",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("visualizer"))
        )
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.1",
    libraryDependencies += "com.raquo" %%% "laminar" % "17.2.1",
    libraryDependencies += "org.scalactic" %%% "scalactic" % "3.2.20",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
  )
  .dependsOn(runner.js, simulation.js)
