package de.htwg.se.minesweeper.database.mongoimpl

import de.htwg.se.minesweeper.database.GameStateDao
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.model.GameState.{gameStateFromJSON, gameStateToJSON}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class MongoGameStateDao(database: MongoDatabase)(implicit ec: ExecutionContext) extends GameStateDao {

  private val collection: MongoCollection[Document] = database.getCollection("gamestates")

  override def save(gameState: GameState, id: String): Future[Unit] = {
    val json = gameStateToJSON(gameState).toString()
    val doc = Document("_id" -> id, "json" -> json)
    val options = ReplaceOptions().upsert(true)
    collection.replaceOne(equal("_id", id), doc, options).toFuture().map(_ => ())
  }

  override def load(id: String): Future[Option[GameState]] = {
    collection.find(equal("_id", id)).first().toFutureOption().map {
      case Some(doc) =>
        val jsonStr = doc.getString("json")
        Some(gameStateFromJSON(Json.parse(jsonStr)))
      case None => None
    }
  }
}