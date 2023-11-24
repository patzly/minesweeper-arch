package minesweeper.view

import minesweeper.model.Field
import minesweeper.controller.FieldController
import minesweeper.controller.Event
import minesweeper.observer.Observer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class Tui(controller: FieldController) extends Observer[Event] {
	private var loop = true
	controller.addObserver(this)

	def fieldString(field: Field): String = {
		val (rows, cols) = field.dimension
		val l = cols.toString.length + 2

		val pad = " " * l

		val n1 = pad + (" " * 18) + (10 until cols+1).map(a => a / 10).mkString(" ")
		val n0 = pad + (1 until cols+1).map(a => a % 10).mkString(" ")

		val numbers = if cols % 10 == 0 then n1 + "\n" + n0 else n0

		val lines = pad + "-" * (cols*2 - 1)
		val rowStrings = field.toString.split('\n').zipWithIndex.map((s, i) => (i + 1).toString.padTo(l-1, ' ') + '|' + s).mkString("\n")

		numbers + "\n" + lines + "\n" + rowStrings
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
		e match {
			case Event.Setup(field) => println(fieldString(field))
			case Event.FieldUpdated(field) => println(fieldString(field))
			case Event.Won => {
				println("You won!")
				loop = false
			}
			case Event.Lost => {
				println("You lost!")
				loop = false
			}
			case Event.Exit => {
				println("Bye!")
				loop = false
			}
		}
	}

	def play(): Unit = {
		println(this)
		while loop do {
			processLine(scala.io.StdIn.readLine())
		}
	}
}
