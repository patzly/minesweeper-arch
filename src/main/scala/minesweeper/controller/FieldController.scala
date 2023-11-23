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

	def reveal(x: Int, y: Int): Try[Unit] = execute(RevealCommand(this, x, y))

	private def reveal_impl(x: Int, y: Int): Try[Unit] = {
		if isFirstMove then {
			while field.getCell(x, y) match {
				case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
				case Failure(exception) => return Failure(exception)
			} do {
				field = Field(rows, cols, genbomb)
			}
			isFirstMove = false
		}

		field.withRevealed(x, y) match {
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
		Success(())
	}

	def flag(x: Int, y: Int): Try[Unit] = execute(FlagCommand(this, x, y))

	private def flag_impl(x: Int, y: Int): Try[Unit] = {
		field.withToggledFlag(x, y) match {
			case Success(newField) => {
				field = newField
				Try(notifyObservers(Event.FieldUpdated(field)))
			}
			case Failure(exception) => Failure(exception)
		}
	}

	def exit(): Unit = {
		notifyObservers(Event.Exit)
	}

	private var undoStack: List[Command] = List()
	private var redoStack: List[Command] = List()

	private def execute(command: Command): Try[Unit] = {
		undoStack = command :: undoStack
		command.execute()
	}

	def undo(): Try[Unit] = {
		undoStack match {
			case Nil => Failure(new Exception("Nothing to undo!"))
			case head :: tail => {
				head.undo()
				undoStack = tail
				redoStack = head :: redoStack
				Success(())
			}
		}
	}

	def redo(): Try[Unit] = {
		redoStack match {
			case Nil => Failure(new Exception("Nothing to redo!"))
			case head :: tail => {
				head.redo()
				redoStack = tail
				undoStack = head :: undoStack
				Success(())
			}
		}
	}

	private trait Command {
		def execute(): Try[Unit]
		def undo(): Unit
		def redo(): Try[Unit]
	}

	private class RevealCommand(controller: FieldController, x: Int, y: Int) extends Command {
		private val field = controller.field
		override def execute(): Try[Unit] = controller.reveal_impl(x, y)
		override def undo(): Unit = {
			controller.field = field
			controller.notifyObservers(Event.FieldUpdated(field))
		}
		override def redo(): Try[Unit] = controller.reveal_impl(x, y)
	}

	private class FlagCommand(controller: FieldController, x: Int, y: Int) extends Command {
		override def execute(): Try[Unit] = controller.flag_impl(x, y)
		override def undo(): Unit = controller.flag_impl(x, y)
		override def redo(): Try[Unit] = controller.flag_impl(x, y)
	}
}