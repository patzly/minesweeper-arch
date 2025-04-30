package de.htwg.se

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

@main
def mainObserver(hostArg: String, clientHost: String): Unit = {
  val host = if (hostArg.isEmpty) "0.0.0.0" else hostArg

  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  val server = new ObserverServer(clientHost)
  val bindingFuture = server.run(host, 8081)
  println(s"Server now online. Please navigate to http://" + host + ":8081/")
  Await.result(Future.never, Duration.Inf)
}
