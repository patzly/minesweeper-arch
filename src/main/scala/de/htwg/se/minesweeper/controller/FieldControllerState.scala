package de.htwg.se.minesweeper.controller

import de.htwg.se.minesweeper.model.{Field, FieldFactory}
import scala.util.{Failure, Success, Try}

private abstract class FieldControllerState(controller: FieldController) {
	def reveal(x: Int, y: Int): Try[Unit] = {
		controller.field.withRevealed(x, y) match {
			case Success(value) => controller.field = value
			case Failure(exception) => return Failure(exception)
		}

		controller.notifyObservers(FieldUpdatedEvent(controller.field))

		controller.field.getCell(x, y) match {
			case Success(cell) => {
				if cell.isBomb then {
					controller.notifyObservers(LostEvent())
				} else if controller.field.hasWon then {
					controller.notifyObservers(WonEvent())
				}
			}
			case Failure(exception) => return Failure(exception)
		}
		Success(())
	}
}

private class FirstMoveFieldControllerState(controller: FieldController) extends FieldControllerState(controller) {
	override def reveal(x: Int, y: Int): Try[Unit] = {
		while controller.field.getCell(x, y) match {
			case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
			case Failure(exception) => return Failure(exception)
		} do {
			controller.field = controller.factory.createField()
		}

		controller.undoStack = controller.undoStack.prepended(new RevealCommand(controller, x, y))
		controller.changeState(AnyMoveFieldControllerState(controller))

		super.reveal(x, y)
	}
}

private class AnyMoveFieldControllerState(controller: FieldController) extends FieldControllerState(controller) {

}