package de.htwg.se.minesweeper.controller.restClientController

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.observer.Observable
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.se.minesweeper.model.GameState.gameStateFromJSON
import de.htwg.se.minesweeper.model.fieldComponent.fieldFromJSON
import de.htwg.se.util.HttpClient
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

def eventFromJson(event: JsValue): Event = {
  val eventType = (event \ "event").as[String]

  eventType match {
    case "setup" => SetupEvent()
    case "startGame" => StartGameEvent(fieldFromJSON((event \ "field").as[JsValue]))
    case "fieldUpdated" => FieldUpdatedEvent(fieldFromJSON((event \ "field").as[JsValue]))
    case "won" => WonEvent()
    case "lost" => LostEvent()
    case "exit" => ExitEvent()
    case _ => throw new IllegalArgumentException("Unknown event type")
  }
}

private class MinesweeperServerObserver(observable: Observable[Event]) extends Observable[Event] {
    implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
    implicit val executionContext: ExecutionContext = system.dispatcher

    private var gameState: GameState = null
    def getGameState = gameState

    def run(port: Int): Future[ServerBinding] = {
      val serverBinding = Http()
        .newServerAt("localhost", port)
        .bind(routes)

      CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "shutdown-server") { () =>

        shutdown(serverBinding)
      }

      serverBinding.onComplete {
        case Success(binding) => println("MinesweeperServerObserver -- Http Server is running at \n")
        case Failure(exception) => println("MinesweeperServerObserver -- Http Server failed to start " + exception)
      }
      serverBinding
    }

  def routes: Route = {
    concat(
      post {
        path("update") {
          entity(as[String]) { json =>
            Try {
              println("received event: " + json)
              val ev = eventFromJson(Json.parse(json))
              observable.notifyObservers(ev)
              println("event updated: " + ev)
            } match {
              case Failure(exception) =>
                println("Failed to parse event: " + exception.getMessage)
                complete(StatusCodes.BadRequest)
              case Success(_) =>
                println("event updated successfully")
                complete(StatusCodes.OK)
            }
            complete(StatusCodes.OK)
          }
        }
      },
      post {
        path("updateState") {
          entity(as[String]) { json =>
            println("received game state: " + json)
            val state = gameStateFromJSON(Json.parse(json))
            gameState = state
            println("game state updated: " + gameState)
            complete(StatusCodes.OK)
          }
        }
      }
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

class RestClientController(port: Int) extends Observable[Event] with ControllerInterface {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val url = "http://localhost:8080/minesweeper"
  private val observer = MinesweeperServerObserver(this)
  private val http = new HttpClient
  private val bindingFuture = {
    val future = observer.run(port)
    http.postRequest(url + "/registerClient", Json.obj(
      "clientUrl" -> ("http://localhost:" + port.toString)
    ).toString) match {
        case Success(_) =>
            println("Client registered successfully")
        case Failure(exception) =>
            println("Failed to register client: " + exception.getMessage)
    }
    future
  }


  override def loadGame(path: String): Try[Unit] = http.postRequest(url + "/loadGame", s"""{"path":"$path"}""")

  override def saveGame(path: String): Try[Unit] = http.postRequest(url + "/saveGame", s"""{"path":"$path"}""")

  override def getGameState: GameState = observer.getGameState

  override def flag(x: Int, y: Int): Try[Unit] = http.postRequest(url + "/flag", s"""{"x":$x,"y":$y}""")

  override def reveal(x: Int, y: Int): Try[Unit] = http.postRequest(url + "/reveal", s"""{"x":$x,"y":$y}""")

  override def redo(): Try[Unit] = http.getRequest(url + "/redo")

  override def undo(): Try[Unit] = http.getRequest(url + "/undo")

  override def exit(): Unit = http.postRequest(url + "/deregisterClient", Json.obj(
    "clientUrl" -> ("http://localhost:" + port.toString)
  ).toString)

  override def setup(): Unit = {
    println("setting up server")
    http.getRequest(url + "/setup")
    println("set up server")
  }

  override def startGame(width: Int, height: Int, bomb_chance: Float, undos: Int): Unit =
    http.postRequest(url + "/startGame", Json.obj(
        "width" -> width,
        "height" -> height,
        "bomb_chance" -> bomb_chance,
        "undos" -> undos
        ).toString
    )
}
