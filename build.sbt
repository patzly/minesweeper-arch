ThisBuild / scalaVersion := "3.3.1"

lazy val model = (project in file("model"))
lazy val fileio = (project in file("FileIOComponent")).dependsOn(model)
lazy val util = (project in file("util"))
lazy val ObserverService =
  (project in file("ObserverService")).dependsOn(model, util)
lazy val minesweeper =
  (project in file("minesweeper"))
    .settings(name := "minesweeper")
    .dependsOn(model, fileio, util)

lazy val root = project
  .in(file("."))
  .settings(
    name := "minesweeper",
    version := "1.0.0"
  )
  .aggregate(minesweeper, model, fileio)
