package minesweeper.view

import minesweeper.controller.FieldController

enum TUIState {
	case Continue
	case Won
	case Lost
	case Invalid(string: String)
	case Exit
}

class Tui(controller: FieldController) {
	override def toString: String =
		val (cols, rows) = controller.field.dimension
		val l = rows.toString.length + 1
		" " * (l+1) + (1 until cols+1).mkString(" ") + "\n"
		  + " " * (l+1) + "-" * (cols*2 - 1) + "\n"
		  + controller.field.toString.split('\n').zipWithIndex.map((s, i) => (i + 1).toString.padTo(l, ' ') + '|' + s).mkString("\n")

	def processLine(line: String): TUIState = {
		line match {
			case "q" => return TUIState.Exit
			case _ =>
				val inputs = line.split(" ").toList
				if inputs.length < 2 then {
				    return TUIState.Invalid("Invalid input: Format is <column> <row>!")
				}

				val (x, y) = (inputs(0).toIntOption, inputs(1).toIntOption) match {
				    case (Some(x), Some(y)) => (x-1, y-1)
				    case _ => return TUIState.Invalid("Invalid input: Please enter numbers!")
				}

				println(s"Selected ($x, $y)")
				if controller.field.isInBounds(x, y) && controller.field.getCell(x, y).isRevealed then {
					return TUIState.Invalid("Cell already revealed!")
				}

				try {
				    controller.reveal(x, y)
				} catch {
				    case e: IndexOutOfBoundsException => return TUIState.Invalid(e.getMessage)
				}

				if controller.field.getCell(x, y).isBomb then {
					return TUIState.Lost
				}
				if controller.field.hasWon then {
					return TUIState.Won
				}
		}
		TUIState.Continue
	}
}
