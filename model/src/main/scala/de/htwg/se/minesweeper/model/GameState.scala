package de.htwg.se.minesweeper.model

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

import scala.util.{Failure, Success, Try}

// Represents the state of the game
case class GameState(
    undos: Int,
    maxUndos: Int,
    field: FieldInterface,
    bombChance: Float,
    width: Int,
    height: Int,
    firstMove: Boolean = true,
    undoFields: List[FieldInterface] = List.empty,
    redoFields: List[FieldInterface] = List.empty
) {
  def cantRedo: Boolean = redoFields.isEmpty
  def cantUndo: Boolean = undos <= 0 || undoFields.isEmpty
  // returns a new GameState with the latest move undone
  def undo: Try[GameState] = {
    if cantUndo then Failure(new IllegalStateException("Cannot undo"))
    else
      Success(
        copy(
          undos = undos - 1,
          field = undoFields.head,
          undoFields = undoFields.tail,
          redoFields = this.field :: redoFields
        )
      )
  }
  // returns the GameState that was most recently undone
  def redo: Try[GameState] = {
    if cantRedo then Failure(new IllegalStateException("Cannot redo"))
    else
      Success(
        copy(
          field = redoFields.head,
          undoFields = this.field :: undoFields,
          redoFields = redoFields.tail
        )
      )
  }

  // returns a new GameState with the field updated
  def updateField(newField: FieldInterface): GameState = {
    copy(
      field = newField,
      firstMove = false,
      undoFields = this.field :: undoFields,
      redoFields = List.empty
    )
  }
}

object GameState {
  private def cellToJSON(cell: Cell): JsObject = {
    Json.obj(
      "isRevealed" -> cell.isRevealed,
      "isBomb" -> cell.isBomb,
      "isFlagged" -> cell.isFlagged,
      "nearbyBombs" -> cell.nearbyBombs
    )
  }

  private def cellFromJSON(json: JsValue): Cell = {
    val isRevealed = (json \ "isRevealed").as[Boolean]
    val isBomb = (json \ "isBomb").as[Boolean]
    val isFlagged = (json \ "isFlagged").as[Boolean]
    val nearbyBombs = (json \ "nearbyBombs").as[Int]
    Cell(isRevealed, isBomb, isFlagged, nearbyBombs)
  }

  private def fieldToJSON(field: FieldInterface): JsObject = {
    Json.obj(
      "matrix" -> Json.toJson(
        for y <- 0 until field.dimension._2
          yield Json.toJson(field.getRow(y).get.map(cellToJSON))
      )
    )
  }

  private def fieldFromJSON(json: JsValue): FieldInterface = {
    val matrix = (json \ "matrix").as[JsArray]
    val cells = matrix.value
      .map(row => row.as[JsArray].value.map(cellFromJSON).toVector)
      .toVector
    FieldInterface.fromMatrix(cells)
  }

   def gameStateToJSON(gameState: GameState): JsObject = {
    Json.obj(
      "undos" -> gameState.undos,
      "maxUndos" -> gameState.maxUndos,
      "field" -> fieldToJSON(gameState.field),
      "bombChance" -> gameState.bombChance,
      "width" -> gameState.width,
      "height" -> gameState.height,
      "firstMove" -> gameState.firstMove,
      "undoFields" -> Json.toJson(gameState.undoFields.map(fieldToJSON)),
      "redoFields" -> Json.toJson(gameState.redoFields.map(fieldToJSON))
    )
  }

  def gameStateFromJSON(json: JsValue): GameState = {
    val undos = (json \ "undos").as[Int]
    val maxUndos = (json \ "maxUndos").as[Int]
    val field = fieldFromJSON((json \ "field").get)
    val bombChance = (json \ "bombChance").as[Float]
    val width = (json \ "width").as[Int]
    val height = (json \ "height").as[Int]
    val firstMove = (json \ "firstMove").as[Boolean]
    val undoFields =
      (json \ "undoFields").as[JsArray].value.map(fieldFromJSON).toList
    val redoFields =
      (json \ "redoFields").as[JsArray].value.map(fieldFromJSON).toList
    GameState(
      undos,
      maxUndos,
      field,
      bombChance,
      width,
      height,
      firstMove,
      undoFields,
      redoFields
    )
  }
}
