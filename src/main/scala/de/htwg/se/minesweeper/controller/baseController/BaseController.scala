package de.htwg.se.minesweeper.controller.baseController

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.observer._
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.model.fieldComponent.{FieldFactory, FieldInterface}

import com.google.inject.Inject
import com.google.inject.name.Named

class BaseController @Inject() (@Named("undos") val factory: FieldFactory) extends Observable[Event] with ControllerInterface {
	private[baseController] var width: Int = 0
	private[baseController] var height: Int = 0
	private[baseController] var bomb_chance: Float = 0
	private var maxUndos = 0
	private var undos = 0

	private[baseController] var field: FieldInterface = factory.createField(0, 0, 0)
	private[baseController] var state: BaseControllerState = FirstMoveBaseControllerState(this)

	override def getMaxUndos: Int = maxUndos
	override def getUndos: Int = undos
	override def getField: FieldInterface = field

	override def getBombChance: Float = bomb_chance

	private[baseController] def changeState(newState: BaseControllerState): Unit = {
		state = newState
	}

	override def setup(): Unit = {
		state = FirstMoveBaseControllerState(this)
		undoStack = List.empty
		redoStack = List.empty
		field = factory.createField(width, height, bomb_chance)
		notifyObservers(SetupEvent())
	}

	override def startGame(width: Int, height: Int, bomb_chance: Float, maxUndos: Int): Unit = {
		this.width = width
		this.height = height
		this.bomb_chance = bomb_chance
		this.maxUndos = maxUndos
		undos = maxUndos
		undoStack = undoStack.empty
		redoStack = redoStack.empty
		field = factory.createField(width, height, bomb_chance)
		state = FirstMoveBaseControllerState(this)
		notifyObservers(StartGameEvent(field))
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
		command.execute() match {
			case Success(_) => {
				undoStack = command :: undoStack
				redoStack = List.empty
				Success(())
			}
			case Failure(exception) => Failure(exception)
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

	override def cantRedo: Boolean = redoStack.isEmpty
	override def cantUndo: Boolean = undos <= 0 || undoStack.isEmpty
}