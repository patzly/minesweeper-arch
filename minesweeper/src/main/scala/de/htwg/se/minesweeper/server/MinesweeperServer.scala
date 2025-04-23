package de.htwg.se.minesweeper.server

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.model.fieldComponent.fieldToJSON
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.htwg.se.minesweeper.model.GameState.gameStateToJSON
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.server.MinesweeperServer.getClass
import play.api.libs.json.{JsValue, Json}
import concurrent.duration.DurationInt

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

def eventToJson(event: Event): JsValue = {
  event match {
    case _: SetupEvent => Json.obj("event" -> "setup")
    case StartGameEvent(field) => Json.obj(
      "event" -> "startGame",
      "field" -> fieldToJSON(field),
    )
    case FieldUpdatedEvent(field) => Json.obj(
      "event" -> "fieldUpdated",
      "field" -> fieldToJSON(field),
    )
    case _: WonEvent => Json.obj("event" -> "won")
    case _: LostEvent => Json.obj("event" -> "lost")
    case _: ExitEvent => Json.obj("event" -> "exit")
  }
}

class MinesweeperRoutes(controller: ControllerInterface) extends Observer[Event] {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val http = Http(system)

  private var clients = Set.empty[String]

  controller.addObserver(this)

  override def update(e: Event): Unit = {
    println("MinesweeperServer -- update: " + e)
    for (clientUrl <- clients) {
      postRequest(clientUrl + "/updateState", gameStateToJSON(controller.getGameState).toString)
      postRequest(clientUrl + "/update", eventToJson(e).toString)
    }
  }

  def routes: Route = {
    concat (
      registerClient,
      deregisterClient,
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

  def registerClient: Route = post {
    path("registerClient") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val clientUrl: String = (jsonValue \ "clientUrl").as[String]
        println("Registering client: " + clientUrl)
        clients = clients + clientUrl
        complete(StatusCodes.OK)
      }
    }
  }

  def deregisterClient: Route = post {
    path("deregisterClient") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val clientUrl: String = (jsonValue \ "clientUrl").as[String]
        println("Registering client: " + clientUrl)
        clients = clients.filterNot(_ == clientUrl)
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
  // loads/saves the game
  // in case of load a FieldUpdatedEvent is sent to all observers
  //  def loadGame(path: String): Try[Unit]
  //  def saveGame(path: String): Try[Unit]


  private def getRequest(url: String, timeout: Duration = 10.seconds) = Try[Unit] {
    sendRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = url,
    ), timeout)
  }

  private def postRequest(url: String, body: String, timeout: Duration = 10.seconds): Try[Unit] = {
    sendRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, body)
    ), timeout)
  }

  private def sendRequest(req: HttpRequest, timeout: Duration): Try[Unit] = {
    println("sending request: " + req.toString)
    Await.ready(http.singleRequest(req).flatMap { response =>
      response.status match
        case StatusCodes.OK =>
          println("Request succeeded: " + response.status)
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
      case Success(binding) => println("MinesweeperServer -- Http Server is running at \n")
      case Failure(exception) => println("MinesweeperServer -- Http Server failed to start " + exception)
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
