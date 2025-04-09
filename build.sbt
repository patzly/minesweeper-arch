ThisBuild / scalaVersion := "3.3.1"

lazy val model = (project in file("model"))
lazy val fileio = (project in file("FileIOComponent")).dependsOn(model)
lazy val minesweeper =
  (project in file("minesweeper")).dependsOn(model, fileio)

lazy val root = project
  .in(file("."))
  .settings(
    name := "minesweeper",
    version := "1.0.0"
  )
  .aggregate(minesweeper, model, fileio)
