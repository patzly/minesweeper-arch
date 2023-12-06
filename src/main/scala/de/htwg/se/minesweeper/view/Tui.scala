package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.model.Field
import de.htwg.se.minesweeper.observer.Observer
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.model.Field
import de.htwg.se.minesweeper.observer.Observer

private trait TuiState {
	def processLine(line: String): Unit
}

private class DefaultTuiState(tui: Tui) extends TuiState {
	def processLine(line: String): Unit = {
		line match {
			case "q" => tui.controller.exit()
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
			case "y" => tui.controller.setup()
			case "n" | "q" => tui.controller.exit()
			case _ => println("Invalid input: Please enter q, y or n!")
		}
	}
}

class Tui(val controller: FieldController) extends Observer[Event] with EventVisitor {
	private var loop = true
	private var state: TuiState = DefaultTuiState(this)
	controller.addObserver(this)

	def fieldString(field: Field): String = {
		val (rows, cols) = field.dimension
		val l = cols.toString.length + 2

		val pad = " " * l

		val numbers = {
			val tens = pad + (" " * 18) + Range.inclusive(10, cols).map(a => a / 10).mkString(" ")
			val ones = pad + Range.inclusive(1, cols).map(a => a % 10).mkString(" ")
			tens + "\n" + ones
		}

		val lines = pad + "-" * (cols*2 - 1)

		val rowStrings = Range.apply(0, field.dimension._1).map(
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
