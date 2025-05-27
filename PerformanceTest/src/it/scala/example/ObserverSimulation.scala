package example
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ObserverSimulation extends Simulation {
  val httpConf = http.baseUrl("http://localhost:8081")

  var clientPort = 1500
  val scn = scenario("Observer Simulation")
    .exec(session => {
      val session2 = session.set("clientUrl", "http://localhost:" + clientPort)
      clientPort += 1
      session2
    })
    .exec(http("Register Client")
      .post("/registerClient").body(StringBody(session => {
        s"""{"clientUrl": "${session("clientUrl").as[String]}"}"""
      })).asJson)

  setUp(
    scn.inject(
      atOnceUsers(10),
      rampUsers(1000) during (10.seconds),
    )
  ).protocols(httpConf)
}
