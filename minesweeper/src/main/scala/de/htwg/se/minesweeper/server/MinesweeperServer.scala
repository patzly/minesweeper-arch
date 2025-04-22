package de.htwg.se.minesweeper.server

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import de.htwg.se.minesweeper.controller.ControllerInterface
import de.htwg.se.minesweeper.model.GameState
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class MinesweeperRoutes(controller: ControllerInterface) {
  def routes: Route = {
    concat (
      getGameState,
      setup,
      startGame,
      reveal,
      flag,
      undo,
      redo,
      exit
    )
  }

  def getGameState: Route = get {
    path("gameState") {
      complete(
        GameState.gameStateToJSON(controller.getGameState).toString
      )
    }
  }

  // sents a SetupEvent to all observers
  def setup: Route = post {
    path("setup") {
      complete(StatusCodes.OK)
    }
  }
  // sents a StartGameEvent to all observers
  def startGame: Route = post {
    path("startGame") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val width: Int = (jsonValue \ "width").as[Int]
        val height: Int = (jsonValue \ "height").as[Int]
        val bomb_chance: Float = (jsonValue \ "bomb_chance").as[Float]
        val undos: Int = (jsonValue \ "undos").as[Int]
        complete(StatusCodes.OK)
      }
    }
  }
  // reveals a cell and sends a FieldUpdatedEvent to all observers
  def reveal: Route = post {
    path("reveal") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val x: Int = (jsonValue \ "x").as[Int]
        val y: Int = (jsonValue \ "y").as[Int]
        complete(StatusCodes.OK)
      }
    }
  }
  // toggles a flag and sends a FieldUpdatedEvent to all observers
  def flag: Route = post {
    path("flag") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val x: Int = (jsonValue \ "x").as[Int]
        val y: Int = (jsonValue \ "y").as[Int]
        complete(StatusCodes.OK)
      }
    }
  }
  // undos/redos the last action and sends a FieldUpdatedEvent to all observers
  def undo: Route = post {
    path("undo") {
      complete(StatusCodes.OK)
    }
  }
  def redo: Route = post {
    path("redo") {
      complete(StatusCodes.OK)
    }
  }
  // sents a ExitEvent to all observers
  def exit: Route = post {
    path("exit") {
      complete(StatusCodes.OK)
    }
  }
  // loads/saves the game
  // in case of load a FieldUpdatedEvent is sent to all observers
  //  def loadGame(path: String): Try[Unit]
  //  def saveGame(path: String): Try[Unit]
}

object MinesweeperServer {
  private[server] implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  private implicit val executionContext: ExecutionContext = system.dispatcher

  def run(controller: ControllerInterface): Future[ServerBinding] = {
    val serverBinding = Http()
      .newServerAt("localhost", 8080)
      .bind(routes(MinesweeperRoutes(controller)))

    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "shutdown-server") { () =>
      shutdown(serverBinding)
    }

    serverBinding.onComplete {
      case Success(binding) => println("Core Service -- Http Server is running at \n")
      case Failure(exception) => println("Core Service -- Http Server failed to start " + exception)
    }
    serverBinding
  }

    private def routes(routes: MinesweeperRoutes): Route =
      pathPrefix("minesweeper") {
        concat(
          routes.routes
        )
      }


  private def shutdown(serverBinding: Future[ServerBinding]): Future[Done] =
    serverBinding.flatMap { binding =>
      binding.unbind().map { _ =>
        system.terminate()
        Done
      }
    }

  }
