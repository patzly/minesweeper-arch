package de.htwg.se.minesweeper.database

import de.htwg.se.minesweeper.model.GameState

import scala.concurrent.Future

trait GameStateDao {
  def save(gameState: GameState): Future[Unit]
  def load(): Future[Option[GameState]]
}