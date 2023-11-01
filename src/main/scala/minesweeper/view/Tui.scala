package minesweeper.view

import minesweeper.controller.FieldController

class Tui(controller: FieldController) {
	private def invalidInput(msg: String): Boolean = {
	    System.err.println(msg)
	    true
	}
	private def invalidInput(): Boolean = invalidInput("Invalid input")

	def processLine(line: String): Boolean = {
		line match {
			case "q" => return false
			case _ => {
				val inputs = line.split(" ").toList
				if inputs.length < 2 then {
				    return invalidInput()
				}
				val (x, y) = (inputs(0).toIntOption, inputs(1).toIntOption) match {
				    case (Some(x), Some(y)) => (x-1, y-1)
				    case _ => return invalidInput()
				}
				println(s"Selected ($x, $y)")
				try {
				    controller.reveal(x, y)
				} catch {
				    case e: IndexOutOfBoundsException => return invalidInput(e.getMessage)
				}
				println(controller.field)
				if controller.field.matrix(y)(x).isBomb then {
					println("You lost!")
					return false
				}
				if controller.field.hasWon then {
					println("You won!")
					return false
				}
			}
		}
		true
	}
}
