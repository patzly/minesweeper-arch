package de.htwg.se

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import de.htwg.se.database.{ClientDao, DatabaseModule}
import de.htwg.se.util.HttpClient
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class ObserverServerRoutes(clientHost: String, clientDao: ClientDao) {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val http = new HttpClient

  def routes: Route = {
    concat(
      registerClient,
      deregisterClient,
      update,
    )
  }

  private def registerClient: Route = post {
    path("registerClient") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val clientUrl: String = (jsonValue \ "clientUrl").as[String].replace("localhost", clientHost)
        println("Registering client: " + clientUrl)
        var result = StatusCodes.OK.intValue
        val future = clientDao.insert(clientUrl)
        future.onComplete({
          case Success(_) => clientDao.list().onComplete({
            case Success(clients) => {
              println("Client registered successfully: " + clients.mkString(", "))
              result = StatusCodes.OK.intValue
            }
            case Failure(exception) => {
              println("Failed to register client: " + exception)
              result = StatusCodes.InternalServerError.intValue
            }
          })
          case Failure(exception) => {
            println("Failed to register client: " + exception)
            result = StatusCodes.InternalServerError.intValue
          }
        })
        Await.ready(future, Duration.Inf)
        complete(StatusCodes.getForKey(result))
      }
    }
  }

  private def deregisterClient: Route = post {
    path("deregisterClient") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val clientUrl: String = (jsonValue \ "clientUrl").as[String]
        println("Deregistering client: " + clientUrl)
        clientDao.delete(clientUrl)
        complete(StatusCodes.OK)
      }
    }
  }

  def update: Route = post {
    path("update") {
      entity(as[String]) { json =>
        val jsonValue = Json.parse(json);
        val gameState = (jsonValue \ "gameState").as[JsValue]
        val event = (jsonValue \ "event").as[JsValue]
        println("Received event: " + event.toString)
        println("Received gameState: " + gameState.toString)
        onSuccess(clientDao.list()) { clients =>
          clients.foreach { clientUrl =>
            http.postRequest(clientUrl + "/updateState", gameState.toString)
            http.postRequest(clientUrl + "/update", event.toString)
          }
          complete(StatusCodes.OK)
        }
      }
    }
  }
}

class ObserverServer(clientHost: String) {
  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val clientDao = DatabaseModule.initSlick("clients.db")
  // private val clientDao = DatabaseModule.initMongo("mongodb://localhost:27017", "observer")(executionContext)
  private val observerServerRoutes = ObserverServerRoutes(clientHost, clientDao)

  def run(host: String, port: Int): Future[ServerBinding] = {
    val serverBinding = Http()
      .newServerAt(host, port)
      .bind(observerServerRoutes.routes)

    CoordinatedShutdown(system).addTask(
      CoordinatedShutdown.PhaseServiceStop,
      "shutdown-server"
    ) {
      () => shutdown(serverBinding)
    }

    serverBinding.onComplete {
      case Success(binding) =>
        println("ObserverServer -- Http Server is running at \n")
      case Failure(exception) =>
        println("ObserverServer -- Http Server failed to start " + exception)
    }
    serverBinding
  }

  private def shutdown(serverBinding: Future[ServerBinding]): Future[Done] =
    serverBinding.flatMap { binding =>
      binding.unbind().map { _ =>
        system.terminate()
        Done
      }
    }
}
