package de.htwg.se.minesweeper

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import de.htwg.se.minesweeper.controller.ControllerInterface

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import com.google.inject.Guice

@main
def main(): Unit = {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  val injector = Guice.createInjector(new MinesweeperModule)
  val controller = injector.getInstance(classOf[ControllerInterface])

  val bindingFuture = de.htwg.se.minesweeper.server.MinesweeperServer.run(controller)
  println(s"Server now online. Please navigate to http://localhost:8080/")
  Await.result(Future.never, Duration.Inf)
}
