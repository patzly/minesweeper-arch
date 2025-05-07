package de.htwg.se.minesweeper.server

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.model.fieldComponent.fieldToJSON
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.se.minesweeper.database.{DatabaseModule, GameStateDao}
import de.htwg.se.minesweeper.model.GameState.gameStateToJSON
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.server.MinesweeperServer.getClass
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import de.htwg.se.util.HttpClient

class MinesweeperRoutes(controller: ControllerInterface, observerUrl: String)
    extends Observer[Event] {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val http = new HttpClient

  controller.addObserver(this)

  override def update(e: Event): Unit = {
    println("MinesweeperServer -- update: " + e)
    val json = Json.obj(
      "event" -> eventToJson(e),
      "gameState" -> gameStateToJSON(controller.getGameState),
    )
    http.postRequest(observerUrl + "/update", json.toString)
  }

  private def eventToJson(event: Event): JsValue = {
    event match {
      case _: SetupEvent => Json.obj("event" -> "setup")
      case StartGameEvent(field) =>
        Json.obj(
          "event" -> "startGame",
          "field" -> fieldToJSON(field)
        )
      case FieldUpdatedEvent(field) =>
        Json.obj(
          "event" -> "fieldUpdated",
          "field" -> fieldToJSON(field)
        )
      case _: WonEvent  => Json.obj("event" -> "won")
      case _: LostEvent => Json.obj("event" -> "lost")
      case _: ExitEvent => Json.obj("event" -> "exit")
    }
  }

  def routes: Route = {
    concat(
      registerClient,
      deregisterClient,
      getGameState,
      setup,
      startGame,
      reveal,
      flag,
      undo,
      redo,
      exit,
      loadGame,
      saveGame
    )
  }

  def registerClient: Route = post {
    path("registerClient") {
      entity(as[String]) { json =>
        http.postRequest(observerUrl + "/registerClient", json)
        complete(StatusCodes.OK)
      }
    }
  }

  def deregisterClient: Route = post {
    path("deregisterClient") {
      entity(as[String]) { json =>
        http.postRequest(observerUrl + "/deregisterClient", json)
        complete(StatusCodes.OK)
      }
    }
  }

  def getGameState: Route = get {
    path("gameState") {
      complete(
        GameState.gameStateToJSON(controller.getGameState).toString
      )
    }
  }

  // sents a SetupEvent to all observers
  def setup: Route = get {
    path("setup") {
      controller.setup()
      complete(StatusCodes.OK)
    }
  }
  // sents a StartGameEvent to all observers
  def startGame: Route = post {
    path("startGame") {
      println("starting game")
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val width: Int = (jsonValue \ "width").as[Int]
        val height: Int = (jsonValue \ "height").as[Int]
        val bomb_chance: Float = (jsonValue \ "bomb_chance").as[Float]
        val undos: Int = (jsonValue \ "undos").as[Int]
        controller.startGame(
          width,
          height,
          bomb_chance,
          undos
        )
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
        controller.reveal(x, y) match {
          case Success(_) =>
            complete(StatusCodes.OK)
          case Failure(exception) =>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
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
        controller.flag(x, y) match {
          case Success(_) =>
            complete(StatusCodes.OK)
          case Failure(exception) =>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }
  // undos/redos the last action and sends a FieldUpdatedEvent to all observers
  def undo: Route = get {
    path("undo") {
      controller.undo() match {
        case Success(_) =>
          complete(StatusCodes.OK)
        case Failure(exception) =>
          complete(StatusCodes.InternalServerError, exception.getMessage)
      }
    }
  }

  def redo: Route = get {
    path("redo") {
      controller.redo() match {
        case Success(_) =>
          complete(StatusCodes.OK)
        case Failure(exception) =>
          complete(StatusCodes.InternalServerError, exception.getMessage)
      }
    }
  }
  // sents a ExitEvent to all observers
  def exit: Route = get {
    path("exit") {
      controller.exit()
      complete(StatusCodes.OK)
    }
  }
  def loadGame: Route = post {
    path("loadGame") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val path: String = (jsonValue \ "path").as[String]
        controller.loadGame(path) match {
          case Success(_) =>
            complete(StatusCodes.OK)
          case Failure(exception) =>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }
  def saveGame: Route = post {
    path("saveGame") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val path: String = (jsonValue \ "path").as[String]
        controller.saveGame(path) match {
          case Success(_) =>
            complete(StatusCodes.OK)
          case Failure(exception) =>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }
}

object MinesweeperServer {
  private[server] implicit val system: ActorSystem = ActorSystem(
    getClass.getSimpleName.init
  )
  private implicit val executionContext: ExecutionContext = system.dispatcher

  def run(controller: ControllerInterface, host: String, port: Int, observerUrl: String): Future[ServerBinding] = {
    val serverBinding = Http()
      .newServerAt(host, port)
      .bind(routes(MinesweeperRoutes(controller, observerUrl)))

    CoordinatedShutdown(system).addTask(
      CoordinatedShutdown.PhaseServiceStop,
      "shutdown-server"
    ) { () =>
      shutdown(serverBinding)
    }

    serverBinding.onComplete {
      case Success(binding) =>
        println("MinesweeperServer -- Http Server is running at \n")
      case Failure(exception) =>
        println("MinesweeperServer -- Http Server failed to start " + exception)
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
