package de.htwg.se.minesweeper.controller.baseController

import de.htwg.se.minesweeper.model.fieldComponent._
import scala.util.{Failure, Success, Try}
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.model.GameState

private abstract class BaseControllerState(controller: BaseController) {
	// returns the field before the reveal
	def reveal(x: Int, y: Int): Try[Unit] = {
		controller.gameState.field.withRevealed(x, y) match {
			case Success(value) => controller.gameState = controller.gameState.updateField(value)
			case Failure(exception) => return Failure(exception)
		}

		controller.notifyObservers(FieldUpdatedEvent(controller.gameState.field))

		// .get because failure isn't possible
		if controller.gameState.field.getCell(x, y).get.isBomb then {
			controller.notifyObservers(LostEvent())
		} else if controller.gameState.field.hasWon then {
			controller.notifyObservers(WonEvent())
		}
		Success(())
	}
	def flag(x: Int, y: Int): Try[Unit] = {
		controller.gameState.field.withToggledFlag(x, y) match {
			case Success(newField) => {
				controller.gameState = controller.gameState.updateField(newField)
				Try(controller.notifyObservers(FieldUpdatedEvent(controller.gameState.field)))
			}
			case Failure(exception) => Failure(exception)
		}
	}
}

private class FirstMoveBaseControllerState(controller: BaseController) extends BaseControllerState(controller) {
	override def reveal(x: Int, y: Int): Try[Unit] = {
		while controller.gameState.field.getCell(x, y) match {
			case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
			case Failure(exception) => return Failure(exception)
		} do {
			controller.gameState = controller.gameState.copy(field = controller.factory.createField(controller.gameState.width, controller.gameState.height, controller.gameState.bombChance))
		}

		controller.changeState(AnyMoveBaseControllerState(controller))
		super.reveal(x, y) // not checked because it can't fail
		Success(())
	}
}

private class AnyMoveBaseControllerState(controller: BaseController) extends BaseControllerState(controller) {

}