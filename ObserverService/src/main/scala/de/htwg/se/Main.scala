package de.htwg.se

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

@main
def mainObserver(): Unit = {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  val bindingFuture = ObserverServer.run
  println(s"Server now online. Please navigate to http://localhost:8081/")
  Await.result(Future.never, Duration.Inf)
}
