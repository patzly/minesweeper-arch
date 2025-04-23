package de.htwg.se.minesweeper.clientMain

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.google.inject.{AbstractModule, Guice}
import de.htwg.se.minesweeper.controller.ControllerInterface
import de.htwg.se.minesweeper.controller.restClientController.RestClientController
import de.htwg.se.minesweeper.model.FileIOComponent.FileIOInterface
import de.htwg.se.minesweeper.model.FileIOComponent.Flexible.FileIO
import de.htwg.se.minesweeper.model.fieldComponent.FieldFactory
import de.htwg.se.minesweeper.model.fieldComponent.field.RandomFieldFactory
import de.htwg.se.minesweeper.view.{Gui, Tui}

import concurrent.duration.DurationInt
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Random

@main
def mainClient2(): Unit = {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext


  val injector = Guice.createInjector(new ClientModule2)
  val controller = injector.getInstance(classOf[ControllerInterface])

  val tui = Tui(controller)
  controller.setup()

  tui.play()

  Await.result(Future.never, Duration.Inf)
}

class ClientModule2 extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FileIOInterface]).to(classOf[FileIO])
    bind(classOf[FieldFactory]).toInstance(RandomFieldFactory(Random()))
    bind(classOf[ControllerInterface]).toInstance(RestClientController(8081))
  }
}
