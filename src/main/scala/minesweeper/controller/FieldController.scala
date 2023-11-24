package minesweeper.controller

import minesweeper.model.{Cell, Field, FieldFactory}
import minesweeper.observer.Observable

import scala.util.Try
import scala.util.Success
import scala.util.Failure

class FieldController(factory: FieldFactory) extends Observable[Event] {
	private var field: Field = factory.createField()
	private var isFirstMove = true

	def setup(): Unit = {
		notifyObservers(SetupEvent(field))
	}

	def reveal(x: Int, y: Int): Try[Unit] = execute(RevealCommand(this, x, y))

	private def reveal_impl(x: Int, y: Int): Try[Unit] = {
		if isFirstMove then {
			while field.getCell(x, y) match {
				case Success(cell) => cell.nearbyBombs != 0 || cell.isBomb
				case Failure(exception) => return Failure(exception)
			} do {
				field = factory.createField()
			}
			isFirstMove = false
		}

		field.withRevealed(x, y) match {
			case Success(value) => field = value
			case Failure(exception) => return Failure(exception)
		}

		notifyObservers(FieldUpdatedEvent(field))

		field.getCell(x, y) match {
			case Success(cell) => {
				if cell.isBomb then {
					notifyObservers(LostEvent())
				} else if field.hasWon then {
					notifyObservers(WonEvent())
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
				Try(notifyObservers(FieldUpdatedEvent(field)))
			}
			case Failure(exception) => Failure(exception)
		}
	}

	def exit(): Unit = {
		notifyObservers(ExitEvent())
	}

	private var undoStack: List[Command] = List()
	private var redoStack: List[Command] = List()

	private def execute(command: Command): Try[Unit] = {
		undoStack = command :: undoStack
		redoStack = List()
		command.execute()
	}

	def undo(): Try[Unit] = {
		undoStack match {
			case Nil => Failure(new NoSuchElementException("Nothing to undo!"))
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
			case Nil => Failure(new NoSuchElementException("Nothing to redo!"))
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
			controller.notifyObservers(FieldUpdatedEvent(field))
		}
		override def redo(): Try[Unit] = controller.reveal_impl(x, y)
	}

	private class FlagCommand(controller: FieldController, x: Int, y: Int) extends Command {
		override def execute(): Try[Unit] = controller.flag_impl(x, y)
		override def undo(): Unit = controller.flag_impl(x, y)
		override def redo(): Try[Unit] = controller.flag_impl(x, y)
	}
}