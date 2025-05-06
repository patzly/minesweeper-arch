val akkaVersion = "2.8.5"
val akkaHttpVersion = "10.5.3"
val slickVersion = "3.6.0"

name := "minesweeper"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.17"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test"
libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24"
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
}
libraryDependencies += "com.google.inject" % "guice" % "7.0.0"
libraryDependencies += "net.codingwell" %% "scala-guice" % "5.1.1"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.3"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
libraryDependencies += "com.typesafe.slick" %% "slick" % slickVersion
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.49.1.0"