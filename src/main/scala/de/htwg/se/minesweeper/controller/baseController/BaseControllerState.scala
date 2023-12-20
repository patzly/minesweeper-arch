package de.htwg.se.minesweeper.controller.baseController

import de.htwg.se.minesweeper.model.fieldComponent._
import scala.util.{Failure, Success, Try}
import de.htwg.se.minesweeper.controller._

private abstract class BaseControllerState(controller: BaseController) {
	// returns the field before the reveal
	def reveal(x: Int, y: Int): Try[FieldInterface] = {
		val field = controller.getField
		controller.getField.withRevealed(x, y) match {
			case Success(value) => controller.field = value
			case Failure(exception) => return Failure(exception)
		}

		controller.notifyObservers(FieldUpdatedEvent(controller.getField))

		// .get because failure isn't possible
		if controller.field.getCell(x, y).get.isBomb then {
			controller.notifyObservers(LostEvent())
		} else if controller.field.hasWon then {
			controller.notifyObservers(WonEvent())
		}
		Success(field)
	}
}

private class FirstMoveBaseControllerState(controller: BaseController) extends BaseControllerState(controller) {
	override def reveal(x: Int, y: Int): Try[FieldInterface] = {
		while controller.field.getCell(x, y) match {
			case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
			case Failure(exception) => return Failure(exception)
		} do {
			controller.field = controller.factory.createField(controller.width, controller.height, controller.bomb_chance)
		}

		controller.changeState(AnyMoveBaseControllerState(controller))
		val field = controller.getField
		super.reveal(x, y) match {
			case Success(_) => Success(field)
			case Failure(exception) => {
				Failure(exception)
			}
		}
	}
}

private class AnyMoveBaseControllerState(controller: BaseController) extends BaseControllerState(controller) {

}