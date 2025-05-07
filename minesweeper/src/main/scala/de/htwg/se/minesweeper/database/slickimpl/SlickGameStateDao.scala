package de.htwg.se.minesweeper.database.slickimpl

import de.htwg.se.minesweeper.database.{GameStateDao, GameStateEntity, GameStates}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api.*
import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.model.GameState.gameStateToJSON
import de.htwg.se.minesweeper.model.GameState.gameStateFromJSON

import scala.concurrent.{ExecutionContext, Future}

class SlickGameStateDao(db: Database)(implicit ec: ExecutionContext) extends GameStateDao {
  private val table = GameStates.table

  override def save(gameState: GameState, id: String): Future[Unit] = {
    val json = gameStateToJSON(gameState).toString()
    val entity = GameStateEntity(id, json)
    db.run(table.insertOrUpdate(entity)).map(_ => ())
  }

  override def load(id: String): Future[Option[GameState]] = {
    db.run(table.filter(_.id === id).result.headOption).map {
      case Some(entity) => Some(gameStateFromJSON(play.api.libs.json.Json.parse(entity.json)))
      case None => None
    }
  }
}
