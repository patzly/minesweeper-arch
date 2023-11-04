package minesweeper.controller

import minesweeper.model.Field
import minesweeper.model.Cell
import minesweeper.observer.Observable

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

	def reveal(x: Int, y: Int): Unit = {
		if isFirstMove then {
			while field.getCell(x, y).nearbyBombs != 0 || field.getCell(x, y).isBomb do {
				field = Field(rows, cols, genbomb)
			}
			isFirstMove = false
		}

		field = field.withRevealed(x, y)
		notifyObservers(Event.FieldUpdated(field))

		if field.getCell(x, y).isBomb then {
			notifyObservers(Event.Lost)
		} else if field.hasWon then {
			notifyObservers(Event.Won)
		}
		()
	}

	def exit(): Unit = {
		notifyObservers(Event.Exit)
	}

	def flag(x: Int, y: Int): Unit = {
		field = field.withToggledFlag(x, y)
		notifyObservers(Event.FieldUpdated(field))
	}
}