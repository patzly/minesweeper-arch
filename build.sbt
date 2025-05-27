import sbtassembly.MergeStrategy

def mergeStrategy(x: String): MergeStrategy = x match {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / assemblyMergeStrategy := mergeStrategy


lazy val model = (project in file("model"))
lazy val fileio = (project in file("FileIOComponent")).dependsOn(model)
lazy val util = (project in file("util"))
lazy val ObserverService =
  (project in file("ObserverService"))
    .enablePlugins(AssemblyPlugin)
    .settings(
      name := "observer",
      sbtassembly.AssemblyKeys.assembly / mainClass := Some("de.htwg.se.mainObserver"),
    )
    .dependsOn(model, util)
lazy val minesweeper =
  (project in file("minesweeper"))
    .enablePlugins(AssemblyPlugin)
    .settings(
      name := "minesweeper",
      sbtassembly.AssemblyKeys.assembly / mainClass := Some("de.htwg.se.minesweeper.main"),
    )
    .dependsOn(model, fileio, util)
lazy val performanceTest = (project in file("PerformanceTest"))
  .enablePlugins(GatlingPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "minesweeper",
    version := "1.0.0"
  )
  .aggregate(minesweeper, model, fileio)
