package example
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MinesweeperSimulation extends Simulation {
  val httpConf = http.baseUrl("http://localhost:8080/minesweeper")

  val setupScn = scenario("Setup")
    .exec(http("Setup Game").get("/setup"))
    .exec(http("Start Game").post("/startGame").body(StringBody("""{"width": 10, "height": 10, "bomb_chance": 0.15, "undos": 3}""")).asJson)
    .exec(http("First Reveal").post("/reveal").body(StringBody("""{"x": 2, "y": 2}""")).asJson)
    .pause(1)

  var clientPort = 1500
  val loadSaveScn = scenario("Save/Load")
    .exec(http("Save Game")
      .post("/saveGame").body(StringBody("""{"path": "testPath"}""")).asJson)
    .exec(http("Load Game")
      .post("/loadGame").body(StringBody("""{"path": "testPath"}""")).asJson)

  setUp(
    setupScn.inject(atOnceUsers(1)),
    loadSaveScn.inject(
      rampUsers(10) during (10.seconds),
      constantUsersPerSec(5) during (10.seconds)
    )
  ).protocols(httpConf)
}
