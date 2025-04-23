package de.htwg.se.minesweeper.controller.restClientController

import concurrent.duration.DurationInt
import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.observer.Observable
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.htwg.se.minesweeper.model.GameState.gameStateFromJSON
import de.htwg.se.minesweeper.model.fieldComponent.fieldFromJSON
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
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
            val ev = eventFromJson(Json.parse(json))
            observable.notifyObservers(ev)
            complete(StatusCodes.OK)
          }
        }
      },
      post {
        path("updateState") {
          entity(as[String]) { json =>
            val state = gameStateFromJSON(Json.parse(json))
            gameState = state
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
  private val http = Http(system)
  private val bindingFuture = {
    postRequest("/registerClient", Json.obj(
      "clientUrl" -> ("http://localhost:" + port.toString)
    ).toString) match {
        case Success(_) =>
            println("Client registered successfully")
        case Failure(exception) =>
            println("Failed to register client: " + exception.getMessage)
    }
    observer.run(port)
  }


  override def loadGame(path: String): Try[Unit] = Success(())

  override def saveGame(path: String): Try[Unit] = Success(())

  override def getGameState: GameState = observer.getGameState

  override def flag(x: Int, y: Int): Try[Unit] = postRequest("/flag", s"""{"x":$x,"y":$y}""")

  override def reveal(x: Int, y: Int): Try[Unit] = postRequest("/reveal", s"""{"x":$x,"y":$y}""")

  override def redo(): Try[Unit] = getRequest("/redo")

  override def undo(): Try[Unit] = getRequest("/undo")

  override def exit(): Unit = getRequest("/exit")

  override def setup(): Unit = {
    println("setting up server")
    getRequest("/setup")
    println("set up server")
  }

  override def startGame(width: Int, height: Int, bomb_chance: Float, undos: Int): Unit =
    postRequest("/startGame", Json.obj(
        "width" -> width,
        "height" -> height,
        "bomb_chance" -> bomb_chance,
        "undos" -> undos
        ).toString
    )


  private def getRequest(path: String, timeout: Duration = 10 seconds) = Try[Unit] {
    sendRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = url + path,
    ), timeout)
  }

  private def postRequest(path: String, body: String, timeout: Duration = 10 seconds): Try[Unit] = {
    sendRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = url + path,
        entity = HttpEntity(ContentTypes.`application/json`, body)
    ), timeout)
  }

  private def sendRequest(req: HttpRequest, timeout: Duration): Try[Unit] = {
    println("sending request: " + req.toString)
    Await.ready(http.singleRequest(req).flatMap { response =>
      response.status match
        case StatusCodes.OK =>
          Future.successful(Success(()))
        case _ =>
          Unmarshal(response.entity).to[String].flatMap { body =>
            val e = new RuntimeException("Request failed: " + body)
            println(e)
            Future.failed(e)
          }
      }, timeout)
    Success(())
  }
}
