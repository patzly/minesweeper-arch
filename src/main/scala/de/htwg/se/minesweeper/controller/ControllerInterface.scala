package de.htwg.se.minesweeper.controller

import de.htwg.se.minesweeper.observer.Observable
import scala.util.Try
import de.htwg.se.minesweeper.model.GameState

// ControllerInterface is the interface for the controller
// views can subscribe to the controller using the observer pattern
trait ControllerInterface extends Observable[Event] {
    def getGameState: GameState

    // sents a SetupEvent to all observers
    def setup(): Unit
    // sents a StartGameEvent to all observers
    def startGame(width: Int, height: Int, bomb_chance: Float, undos: Int): Unit
    // reveals a cell and sends a FieldUpdatedEvent to all observers
    def reveal(x: Int, y: Int): Try[Unit]
    // toggles a flag and sends a FieldUpdatedEvent to all observers
    def flag(x: Int, y: Int): Try[Unit]
    // undos/redos the last action and sends a FieldUpdatedEvent to all observers
    def undo(): Try[Unit]
    def redo(): Try[Unit]
    // sents a ExitEvent to all observers
    def exit(): Unit
    // loads/saves the game
    // in case of load a FieldUpdatedEvent is sent to all observers
    def loadGame(path: String): Try[Unit]
    def saveGame(path: String): Try[Unit]
}
