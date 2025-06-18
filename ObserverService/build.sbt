val akkaVersion = "2.8.5"
val akkaHttpVersion = "10.5.3"
val slickVersion = "3.6.0"

name := "observer"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.17"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test"
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
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "5.4.0" cross CrossVersion.for3Use2_13
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "4.0.0"
