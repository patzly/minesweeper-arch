package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import scala.util.Failure
import scala.util.Success
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.observer.Observer

private trait TuiState {
	def processLine(line: String): Unit
}

private class StartGameState(tui: Tui) extends TuiState {
	println("Welcome to Minesweeper!")
	println("Please enter width, height, bomb chance, and number of undos to start a new game or q to quit.")
	override def processLine(line: String): Unit = {
		line match {
			case "q" | null => tui.controller.exit()
			case _ => {
				val inputs = line.split(" ").toList
				if inputs.length < 4 then {
					return println("Invalid input: Format is <width> <height> <bomb_chance> <undos>!")
				}

				val (width, height, bomb_chance, undos) = (inputs(0).toIntOption, inputs(1).toIntOption, inputs(2).toFloatOption, inputs(3).toIntOption) match {
				    case (Some(width), Some(height), Some(bomb_chance), Some(undos)) => (width, height, bomb_chance, undos)
				    case _ => return println("Invalid input: Please enter numbers!")
				}

				if width == 0 || height == 0 then return println("Invalid input: width or height can't be 0!")

				println(s"Starting game with width=$width, height=$height, bomb_chance=$bomb_chance and undos=$undos")

				tui.controller.startGame(width, height, bomb_chance, undos)
			}
		}
	}
}

private class DefaultTuiState(tui: Tui) extends TuiState {
	override def processLine(line: String): Unit = {
		line match {
			case "q" | null => tui.controller.exit()
			case "menu" => tui.controller.setup()
			case "u" => tui.controller.undo() match {
				case Success(value) => ()
				case Failure(exception) => println(exception.getMessage)
			}
			case "r" => tui.controller.redo() match {
				case Success(value) => ()
				case Failure(exception) => println(exception.getMessage)
			}
			case _ => {
				val inputs = line.split(" ").toList
				if inputs.length < 2 then {
					return println("Invalid input: Format is <column> <row>!")
				}

				val (x, y) = (inputs(0).toIntOption, inputs(1).toIntOption) match {
				    case (Some(x), Some(y)) => (x-1, y-1)
				    case _ => return println("Invalid input: Please enter numbers!")
				}

				if inputs.length == 3 && inputs(2) == "flag" then {
					println(s"Toggle flag for ($x, $y)")

					return tui.controller.flag(x, y) match {
						case Success(value) => ()
						case Failure(exception) => println(exception.getMessage)
					}
				}

				println(s"Selected ($x, $y)")

				tui.controller.reveal(x, y) match {
					case Success(value) => ()
					case Failure(exception) => println(exception.getMessage)
				}
			}
		}
	}
}

class RetryTuiState(tui: Tui) extends TuiState {
	override def processLine(line: String): Unit = {
		line match {
			case "y" => {
				val (width, height) = (tui.controller.getField.dimension)
				tui.controller.startGame(width, height, tui.controller.getBombChance, tui.controller.getMaxUndos)
			}
			case "menu" => tui.controller.setup()
			case "n" | "q" | null => tui.controller.exit()
			case _ => println("Invalid input: Please enter q, y or n!")
		}
	}
}

class Tui(val controller: ControllerInterface) extends Observer[Event] with EventVisitor {
	private var loop = true
	private var state: TuiState = DefaultTuiState(this)
	controller.addObserver(this)

	def fieldString(field: FieldInterface): String = {
		val (cols, rows) = field.dimension
		val l = cols.toString.length + 2

		val pad = " " * l

		val numbers = {
			val tens = pad + (" " * 18) + Range.inclusive(10, cols).map(a => a / 10).mkString(" ")
			val ones = pad + Range.inclusive(1, cols).map(a => a % 10).mkString(" ")
			tens + "\n" + ones
		}

		val lines = pad + "-" * (cols*2 - 1)

		val rowStrings = Range.apply(0, rows).map(
				r => (r+1).toString.padTo(l-1, ' ') + '|' + field.getRow(r).get.mkString(" ")
			)
			.mkString("\n")

		s"$numbers\n$lines\n$rowStrings"
	}

	def processLine(line: String): Unit = {
		state.processLine(line)
	}

	override def update(e: Event): Unit =  {
		e.accept(this)
	}
	
	override def visitSetup(event: SetupEvent): Unit = {
		state = StartGameState(this)
		loop = true
	}

	override def visitStartGame(event: StartGameEvent): Unit = {
		state = DefaultTuiState(this)
		loop = true
		println(fieldString(event.field))
	}

	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
		println(fieldString(event.field))
	}

	override def visitWon(event: WonEvent): Unit = {
		println("You won!")
		println("retry? (y/n)")
		state = new RetryTuiState(this)
	}

	override def visitLost(event: LostEvent): Unit = {
		println("You lost!")
		println("retry? (y/n)")
		state = new RetryTuiState(this)
	}

	override def visitExit(event: ExitEvent): Unit = {
		println("Goodbye!")
		loop = false
	}

	def play(): Unit = {
		while loop do {
			processLine(scala.io.StdIn.readLine())
		}
	}
}
