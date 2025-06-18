package de.htwg.se

import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.client.RequestBuilding.WithTransformation
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.stream.ClosedShape
import akka.stream.scaladsl.*
import de.htwg.se.database.{ClientDao, DatabaseModule}
import de.htwg.se.util.HttpClient
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.{Collections, Properties}
import scala.jdk.CollectionConverters._

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
        val result = doRegister(json, clientDao, clientHost)
        complete(StatusCodes.getForKey(result))
      }
    }
  }


  private def deregisterClient: Route = post {
    path("deregisterClient") {
      entity(as[String]) { json =>
        doDeregister(json, clientDao)
        complete(StatusCodes.OK)
      }
    }
  }

  def update: Route = post {
    path("update") {
      entity(as[String]) { json =>
        doUpdate(json, http, clientDao)
        complete(StatusCodes.OK)

//        onSuccess(clientDao.list()) { clients =>
//          clients.foreach { clientUrl =>
//            http.postRequest(clientUrl + "/updateState", gameState.toString)
//            http.postRequest(clientUrl + "/update", event.toString)
//          }
//          complete(StatusCodes.OK)
//        }
      }
    }
  }

}

class ObserverServer(clientHost: String) {
  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  private implicit val executionContext: ExecutionContext = system.dispatcher
  
  private val http = new HttpClient

  private val clientDao = DatabaseModule.initSlick("clients.db")
  // private val clientDao = DatabaseModule.initMongo("mongodb://localhost:27017", "observer")(executionContext)
  private val observerServerRoutes = ObserverServerRoutes(clientHost, clientDao)

  def run(host: String, port: Int): Future[Unit] = {
      Future {
        val props = Properties()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092") // Replace with reachable Kafka address
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group")
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        val consumer = KafkaConsumer[String, String](props)
        consumer.subscribe(Collections.singletonList("my-topic"))

        println("Listening for messages...")
        while true do
          val records = consumer.poll(java.time.Duration.ofMillis(100)).asScala
          for record <- records do
            record.key() match {
              case "register" => {
                val result = doRegister(record.value(), clientDao, clientHost)
                if (result != StatusCodes.OK.intValue) {
                  println(s"Failed to register client: ${record.value()}")
                } else {
                  println(s"Client registered successfully: ${record.value()}")
                }
              }
              case "deregister" => {
                doDeregister(record.value(), clientDao)
                println(s"Client deregistered: ${record.value()}")
              }
              case "update" => {
                println("updating clients")
                doUpdate(record.value(), http, clientDao)
              }
            }        
      }

    /*
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
     */
  }

  private def shutdown(serverBinding: Future[ServerBinding]): Future[Done] =
    serverBinding.flatMap { binding =>
      binding.unbind().map { _ =>
        system.terminate()
        Done
      }
    }
}

def doRegister(json: String, clientDao: ClientDao, clientHost: String)(implicit ec: ExecutionContext) = {
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
  result
}
def doDeregister(json: String, clientDao: ClientDao) = {
  val jsonValue = Json.parse(json);
  val clientUrl: String = (jsonValue \ "clientUrl").as[String]
  println("Deregistering client: " + clientUrl)
  clientDao.delete(clientUrl)
}

def doUpdate(json: String, http: HttpClient, clientDao: ClientDao)(implicit actorSystem: ActorSystem, ec: ExecutionContext) = {
  val jsonValue = Json.parse(json);
  val gameState = (jsonValue \ "gameState").as[JsValue].toString
  val event = (jsonValue \ "event").as[JsValue].toString

  val graph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val input = builder.add(
      Source.future {
        clientDao.list().andThen {
          case Success(value) =>
            println(s"clientDao.list() succeeded with: $value")
          case Failure(ex) =>
            println(s"clientDao.list() failed: ${ex.getMessage}")
        }
      }
    )
    val flattener = builder.add(Flow[Set[String]].mapConcat(_.toList))
    val gameStateUpdater = builder.add(Flow[String].map { clientUrl =>
      http.postRequest(clientUrl + "/updateState", gameState)
    })
    val eventUpdater = builder.add(Flow[String].map { clientUrl =>
      http.postRequest(clientUrl + "/update", event)
    })

    val broadcast = builder.add(Broadcast[String](2))
    val zip = builder.add(Zip[Try[Unit], Try[Unit]]())

    val output = builder.add(Sink.foreach[(Try[Unit], Try[Unit])]  {
      case (Success(_), Success(_)) => println("Update successful for all clients.")
      case (Failure(exception), _) => println(s"Failed to update game state: ${exception.getMessage}")
      case (_, Failure(exception)) => println(s"Failed to update event: ${exception.getMessage}")
    })

    input ~> flattener ~> broadcast

    broadcast.out(0) ~> gameStateUpdater ~> zip.in0
    broadcast.out(1) ~> eventUpdater ~> zip.in1

    zip.out ~> output
    ClosedShape
  }
  RunnableGraph.fromGraph(graph).run()
}
