package de.htwg.se.minesweeper.controller.baseController

import scala.util.{Try, Success, Failure}
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.observer._
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.model.fieldComponent.{FieldFactory, FieldInterface}
import de.htwg.se.minesweeper.model.GameState
import com.google.inject.Inject


class BaseController @Inject() (val factory: FieldFactory) extends Observable[Event] with ControllerInterface {
	private[baseController] var gameState: GameState = GameState(0, 0, factory.createField(0, 0, 0), 0, 0, 0)

	private[baseController] var state: BaseControllerState = FirstMoveBaseControllerState(this)

	private[baseController] def changeState(newState: BaseControllerState): Unit = {
		state = newState
	}

	override def getGameState: GameState = gameState

	override def setup(): Unit = {
		state = FirstMoveBaseControllerState(this)
		gameState = gameState.copy(field = factory.createField(gameState.width, gameState.height, gameState.bombChance))
		notifyObservers(SetupEvent())
	}

	override def startGame(width: Int, height: Int, bomb_chance: Float, maxUndos: Int): Unit = {
		gameState = GameState(maxUndos, maxUndos, factory.createField(width, height, bomb_chance), bomb_chance, width, height)
		state = FirstMoveBaseControllerState(this)
		notifyObservers(StartGameEvent(gameState.field))
	}

	override def reveal(x: Int, y: Int): Try[Unit] = state.reveal(x, y)
	override def flag(x: Int, y: Int): Try[Unit] = state.flag(x, y)
	override def exit(): Unit = {
		notifyObservers(ExitEvent())
	}

	override def undo(): Try[Unit] = {
		gameState = gameState.undo match {
			case Failure(exception) => return Failure(exception)
			case Success(newState) => newState
		}
		Success(notifyObservers(FieldUpdatedEvent(gameState.field)))
	}

	override def redo(): Try[Unit] = {
		gameState = gameState.redo match {
			case Failure(exception) => return Failure(exception)
			case Success(newState) => newState
		}
		Success(notifyObservers(FieldUpdatedEvent(gameState.field)))
	}
}