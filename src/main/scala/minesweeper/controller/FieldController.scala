package minesweeper.controller

import minesweeper.model.Field
import minesweeper.model.Cell

class FieldController(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
    var field: Field = Field(rows, cols, genbomb)
    private var isFirstMove = true
    def reveal(x: Int, y: Int): Unit = {
        if isFirstMove then {
            while field.getCell(x, y).nearbyBombs != 0 || field.getCell(x, y).isBomb do {
                field = Field(rows, cols, genbomb)
            }
            isFirstMove = false
        }

        field = field.withRevealed(x, y)
        ()
    }
}