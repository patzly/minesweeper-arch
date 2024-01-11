package de.htwg.se.minesweeper.controller

import de.htwg.se.minesweeper.observer.Observable
import scala.util.Try
import de.htwg.se.minesweeper.model.GameState

trait ControllerInterface extends Observable[Event] {
    def getGameState: GameState

    def setup(): Unit
    def startGame(width: Int, height: Int, bomb_chance: Float, undos: Int): Unit
    def reveal(x: Int, y: Int): Try[Unit]
    def flag(x: Int, y: Int): Try[Unit]
    def undo(): Try[Unit]
    def redo(): Try[Unit]
    def exit(): Unit
    def loadGame(path: String): Try[Unit]
    def saveGame(path: String): Try[Unit]
}