/*package de.htwg.se.minesweeper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Try}

class HttpClient {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val http = Http(system)

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
*/
