package minesweeper.controller

import minesweeper.controller.{FieldController, FieldUpdatedEvent, LostEvent, WonEvent}

import scala.util.{Failure, Success, Try}

abstract class FieldControllerState(controller: FieldController) {
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

class FirstMoveFieldControllerState(controller: FieldController) extends FieldControllerState(controller) {
	override def reveal(x: Int, y: Int): Try[Unit] = {
		while controller.field.getCell(x, y) match {
			case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
			case Failure(exception) => return Failure(exception)
		} do {
			controller.field = controller.factory.createField()
		}

		controller.changeState(AnyMoveFieldControllerState(controller))

		super.reveal(x, y)
	}
}

class AnyMoveFieldControllerState(controller: FieldController) extends FieldControllerState(controller) {

}