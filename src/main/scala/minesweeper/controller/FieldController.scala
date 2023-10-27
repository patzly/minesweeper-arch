package minesweeper.controller

import minesweeper.model.Field

class FieldController(var field: Field) {
    def reveal(x: Int, y: Int): Unit = {
        if !field.isInBounds(x, y) then 
            throw new IndexOutOfBoundsException(s"Indices ($x, $y) out of bounds for field of dimension (${field.matrix.length}, ${field.matrix(0).length})")
        field = field.withRevealed(x, y)
        ()
    }
}