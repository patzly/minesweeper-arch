package de.htwg.se.minesweeper.model.fieldComponent

import de.htwg.se.minesweeper.model.*

import scala.util.Try
import de.htwg.se.minesweeper.model.fieldComponent.field.Field
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

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

private[fieldComponent] def cellToJSON(cell: Cell): JsObject = {
  Json.obj(
    "isRevealed" -> cell.isRevealed,
    "isBomb" -> cell.isBomb,
    "isFlagged" -> cell.isFlagged,
    "nearbyBombs" -> cell.nearbyBombs
  )
}

private[fieldComponent] def cellFromJSON(json: JsValue): Cell = {
  val isRevealed = (json \ "isRevealed").as[Boolean]
  val isBomb = (json \ "isBomb").as[Boolean]
  val isFlagged = (json \ "isFlagged").as[Boolean]
  val nearbyBombs = (json \ "nearbyBombs").as[Int]
  Cell(isRevealed, isBomb, isFlagged, nearbyBombs)
}


def fieldToJSON(field: FieldInterface): JsObject = {
  Json.obj(
    "matrix" -> Json.toJson(
      for y <- 0 until field.dimension._2
        yield Json.toJson(field.getRow(y).get.map(cellToJSON))
    )
  )
}

def fieldFromJSON(json: JsValue): FieldInterface = {
  val matrix = (json \ "matrix").as[JsArray]
  val cells = matrix.value
    .map(row => row.as[JsArray].value.map(cellFromJSON).toVector)
    .toVector
  FieldInterface.fromMatrix(cells)
}
