package minesweeper.view

import minesweeper.controller.FieldController

class Tui(controller: FieldController) {
    def invalidInput(): Boolean = {
        System.err.println("Invalid input")
        true
    }

    def processLine(line: String): Boolean = {
        line match {
            case "q" => return false
            case _ => {
                val inputs = line.split(" ").toList
                if inputs.length < 2 then {
                    return invalidInput()
                }
                val (x, y) = (inputs(0).toIntOption, inputs(1).toIntOption) match {
                    case (Some(x), Some(y)) => (x, y)
                    case _ => return invalidInput()
                }
                println(s"Selected ($x, $y)")
                controller.reveal(x-1, y-1)
                println(controller.field)
                }
        }
        true
    }
}
