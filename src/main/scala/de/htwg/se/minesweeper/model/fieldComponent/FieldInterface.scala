package de.htwg.se.minesweeper.model.fieldComponent

import de.htwg.se.minesweeper.model._
import scala.util.Try

trait FieldInterface {
    def getCell(x: Int, y: Int): Try[Cell]
    def getRow(y: Int): Try[Vector[Cell]]
    def dimension: (Int, Int)
    def withRevealed(x: Int, y: Int): Try[FieldInterface]
    def withToggledFlag(x: Int, y: Int): Try[FieldInterface]
    def hasWon: Boolean
    def countNearbyMines(x: Int, y: Int): Try[Int]
}