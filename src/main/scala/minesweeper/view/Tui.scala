package minesweeper.view

import minesweeper.model.Field
import minesweeper.controller.*
import minesweeper.observer.Observer
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import minesweeper.controller.SetupEvent

class Tui(controller: FieldController) extends Observer[Event] with EventVisitor {
	private var loop = true
	controller.addObserver(this)

	def fieldString(field: Field): String = {
		val (rows, cols) = field.dimension
		val l = cols.toString.length + 2

		val pad = " " * l

		val numbers = {
			val tens = pad + (" " * 18) + Range.inclusive(10, cols).map(a => a / 10).mkString(" ")
			val ones = pad + Range.inclusive(1, cols).map(a => a % 10).mkString(" ")
			if (cols % 10 == 0) tens + "\n" + ones else ones
		}

		val lines = pad + "-" * (cols*2 - 1)
		
		val field_lines = field.toString.split('\n')
		val rowStrings = field_lines.zipWithIndex.map(
				(s, i) => (i + 1).toString.padTo(l-1, ' ') + '|' + s
			)
			.mkString("\n")

		s"$numbers\n$lines\n$rowStrings"
	}

	def processLine(line: String): Unit = {
		line match {
			case "q" => controller.exit()
			case "u" => controller.undo() match {
				case Success(value) => ()
				case Failure(exception) => println(exception.getMessage)
			}
			case "r" => controller.redo() match {
				case Success(value) => ()
				case Failure(exception) => println(exception.getMessage)
			}
			case _ =>
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

					return controller.flag(x, y) match {
						case Success(value) => ()
						case Failure(exception) => println(exception.getMessage)
					}
				}

				println(s"Selected ($x, $y)")

				controller.reveal(x, y) match {
					case Success(value) => ()
					case Failure(exception) => println(exception.getMessage)
				}
		}
	}

	override def update(e: Event): Unit =  {
		e.accept(this)
	}
	
	override def visitSetup(event: SetupEvent): Unit = {
		println(fieldString(event.field))
	}

	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
		println(fieldString(event.field))
	}

	override def visitWon(event: WonEvent): Unit = {
		println("You won!")
		loop = false
	}

	override def visitLost(event: LostEvent): Unit = {
		println("You lost!")
		loop = false
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
