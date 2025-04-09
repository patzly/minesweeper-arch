ThisBuild / scalaVersion := "3.3.1"

lazy val model = (project in file("model"))
lazy val minesweeper = (project in file("minesweeper")).dependsOn(model)

lazy val root = project
  .in(file("."))
  .settings(
    name := "minesweeper",
    version := "1.0.0",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.17",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24",
    libraryDependencies ++= {
      // Determine OS version of JavaFX binaries
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")   => "linux"
        case n if n.startsWith("Mac")     => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "16" classifier osName)
    },
    libraryDependencies += "com.google.inject" % "guice" % "7.0.0",
    libraryDependencies += "net.codingwell" %% "scala-guice" % "5.1.1",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.3"
  )
  .aggregate(minesweeper, model)
