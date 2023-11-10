package minesweeper.controller

import minesweeper.model.Field
import minesweeper.model.Cell
import minesweeper.observer.Observable

import scala.util.Try
import scala.util.Success
import scala.util.Failure

enum Event {
	case Setup(field: Field)
	case FieldUpdated(field: Field)
	case Won
	case Lost
	case Exit
}

class FieldController(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) extends Observable[Event] {
	private var field: Field = Field(rows, cols, genbomb)
	private var isFirstMove = true

	def setup(): Unit = {
		notifyObservers(Event.Setup(field))
	}

	def reveal(x: Int, y: Int): Try[Unit] = {
		if isFirstMove then {
			while field.getCell(x, y) match {
				case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
				case Failure(exception) => return Failure(exception)
			} do {
				field = Field(rows, cols, genbomb)
			}
			isFirstMove = false
		}

		val withRevealed = field.withRevealed(x, y)
		withRevealed match {
			case Success(value) => field = value
			case Failure(exception) => return Failure(exception)
		}

		notifyObservers(Event.FieldUpdated(field))

		field.getCell(x, y) match {
			case Success(cell) => {
				if cell.isBomb then {
					notifyObservers(Event.Lost)
				} else if field.hasWon then {
					notifyObservers(Event.Won)
				}
			}
			case Failure(exception) => return Failure(exception)
		}
		return Success(())
	}

	def exit(): Unit = {
		notifyObservers(Event.Exit)
	}

	def flag(x: Int, y: Int): Try[Unit] = {
		field.withToggledFlag(x, y) match {
			case Success(newField) => {
				field = newField
				Try(notifyObservers(Event.FieldUpdated(field)))
			}
			case Failure(exception) => return Failure(exception)
		}
	}
}