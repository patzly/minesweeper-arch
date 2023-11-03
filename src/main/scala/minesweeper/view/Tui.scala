package minesweeper.view

import minesweeper.controller.FieldController

class Tui(controller: FieldController) {
	private def invalidInput(msg: String): Boolean = {
	    System.err.println(msg)
	    true
	}

	private def invalidInput(): Boolean = invalidInput("Invalid input")

	override def toString: String =
		val (cols, rows) = controller.field.dimension
		val l = rows.toString.length + 1
		" " * (l+1) + (1 until cols).mkString(" ") + "\n" 
		  + " " * (l+1) + "-" * cols*2 + "\n"
		  + controller.field.toString.split('\n').zipWithIndex.map((s, i) => (i + 1).toString.padTo(l, ' ') + '|' + s).mkString("\n")

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
				if controller.field.isInBounds(x, y) && controller.field.getCell(x, y).isRevealed then {
					return invalidInput("Cell already revealed")
				}

				try {
				    controller.reveal(x, y)
				} catch {
				    case e: IndexOutOfBoundsException => return invalidInput(e.getMessage)
				}

				println(this)

				if controller.field.getCell(x, y).isBomb then {
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
