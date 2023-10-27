package minesweeper.controller

import minesweeper.model.Field

class FieldController(var field: Field) {
    def reveal(x: Int, y: Int): Unit = {
        field = field.withRevealed(x, y)
        ()
    }
}