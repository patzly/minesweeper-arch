package de.htwg.se.minesweeper.controller.baseController

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.observer._
import de.htwg.se.minesweeper.controller._

class BaseController(val base_undos: Int, val factory: FieldFactory) extends Observable[Event] with ControllerInterface {
	private[baseController] var field: Field = factory.createField()
	private[baseController] var state: BaseControllerState = FirstMoveBaseControllerState(this)
	private var undos = base_undos

	override def getUndos: Int = undos
	override def getField: Field = field

	private[baseController] def changeState(newState: BaseControllerState): Unit = {
		state = newState
	}

	override def setup(): Unit = {
		undos = base_undos
		state = FirstMoveBaseControllerState(this)
		undoStack = List.empty
		redoStack = List.empty
		field = factory.createField()
		notifyObservers(SetupEvent(field))
	}

	override def reveal(x: Int, y: Int): Try[Unit] = execute(RevealCommand(this, x, y))

	override def flag(x: Int, y: Int): Try[Unit] = execute(FlagCommand(this, x, y))

	private[baseController] def flag_impl(x: Int, y: Int): Try[Unit] = {
		field.withToggledFlag(x, y) match {
			case Success(newField) => {
				field = newField
				Try(notifyObservers(FieldUpdatedEvent(field)))
			}
			case Failure(exception) => Failure(exception)
		}
	}

	override def exit(): Unit = {
		notifyObservers(ExitEvent())
	}

	private[baseController] var undoStack: List[Command] = List.empty
	private[baseController] var redoStack: List[Command] = List.empty

	private def execute(command: Command): Try[Unit] = {
		undoStack = command :: undoStack
		redoStack = List.empty
		command.execute() match {
			case Success(_) => Success(())
			case Failure(exception) => {
				undoStack = undoStack.tail
				Failure(exception)
			}
		}
	}

	override def undo(): Try[Unit] = {
		undoStack match {
			case Nil => Failure(new NoSuchElementException("Nothing to undo!"))
			case head :: tail => {
				if undos <= 0 then return Failure(new RuntimeException("No more undo's left!"))
				head.undo()
				undoStack = tail
				redoStack = head :: redoStack
				undos -= 1
				Success(())
			}
		}
	}

	override def redo(): Try[Unit] = {
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
}