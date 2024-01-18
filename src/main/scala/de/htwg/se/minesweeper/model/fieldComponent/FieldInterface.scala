package de.htwg.se.minesweeper.model.fieldComponent

import de.htwg.se.minesweeper.model._
import scala.util.Try
import de.htwg.se.minesweeper.model.fieldComponent.field.Field

trait FieldInterface {
    def getCell(x: Int, y: Int): Try[Cell]
    def getRow(y: Int): Try[Vector[Cell]]
    def dimension: (Int, Int)
    // returns a new Field with the cell at (x, y) and its neighbours revealed recursively
    def withRevealed(x: Int, y: Int): Try[FieldInterface]
    // returns a new Field with the cell at (x, y) flagged
    def withToggledFlag(x: Int, y: Int): Try[FieldInterface]
    def hasWon: Boolean
    // returns the number of bombs in the 8 neighbouring cells
    def countNearbyMines(x: Int, y: Int): Try[Int]
}

object FieldInterface {
    // returns a new Field from the cell matrix
    def fromMatrix(matrix: Vector[Vector[Cell]]): FieldInterface = Field(matrix)
}
