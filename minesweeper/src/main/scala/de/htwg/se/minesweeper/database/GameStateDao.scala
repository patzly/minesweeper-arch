package de.htwg.se.minesweeper.database

import de.htwg.se.minesweeper.model.FileIOComponent.FileIOInterface
import de.htwg.se.minesweeper.model.GameState

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait GameStateDao {
  def save(gameState: GameState, id: String): Future[Unit]
  def load(id: String): Future[Option[GameState]]
}

class FileIOGameStateDao(dao: GameStateDao)(implicit executionContext: ExecutionContext) extends FileIOInterface {
  override def load(path: String): Try[GameState] = {
    Await.result(dao.load(path), scala.concurrent.duration.Duration.Inf) match {
      case Some(gameState) => Success(gameState)
      case None            => Failure(new Exception("No game state found"))
    }
  }

    override def save(gameState: GameState, path: String): Try[Unit] = {
      Await.result(dao.save(gameState, path), scala.concurrent.duration.Duration.Inf)
      Success(())
    }
}
