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
		val (rows, cols) = controller.field.dimension
		val l = cols.toString.length + 2

		val pad = " " * l

		val n1 = pad + (" " * 18) + (10 until cols+1).map(a => a / 10).mkString(" ")
		val n0 = pad + (1 until cols+1).map(a => a % 10).mkString(" ")

		val numbers = if cols % 10 == 0 then n1 + "\n" + n0 else n0

		val lines = pad + "-" * (cols*2 - 1)
		val rowStrings = controller.field.toString.split('\n').zipWithIndex.map((s, i) => (i + 1).toString.padTo(l-1, ' ') + '|' + s).mkString("\n")

		numbers + "\n" + lines + "\n" + rowStrings

	def processLine(line: String): TUIState = {
		line match {
			case "q" => return TUIState.Exit
			case _ =>
				val inputs = line.split(" ").toList
				if inputs.length < 2 then {
				    return TUIState.Invalid("Invalid input: Format is <column> <row> ['flag']!")
				}

				val (x, y) = (inputs(0).toIntOption, inputs(1).toIntOption) match {
				    case (Some(x), Some(y)) => (x-1, y-1)
				    case _ => return TUIState.Invalid("Invalid input: Please enter numbers!")
				}

				if inputs.length == 3 && inputs(2) == "flag" then {
					println(s"Toggle flag for ($x, $y)")
					try {
						controller.flag(x, y)
					} catch {
						case e: IndexOutOfBoundsException => return TUIState.Invalid(e.getMessage)
					}
					return TUIState.Continue
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
