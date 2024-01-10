package de.htwg.se.minesweeper.model.FileIOComponent.JSON

import de.htwg.se.minesweeper.model.* 
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.model.FileIOComponent.FileIOInterface
import scala.util.Try
import play.api.libs.json._
import de.htwg.se.minesweeper.model.FileIOComponent.FileExtension

class FileIO extends FileIOInterface {
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
        val cells = matrix.value.map(row => row.as[JsArray].value.map(cellFromJSON).toVector).toVector
        FieldInterface.fromMatrix(cells)
    }

    private def gameStateToJSON(gameState: GameState): JsObject = {
        Json.obj(
            "undos" -> gameState.undos,
            "maxUndos" -> gameState.maxUndos,
            "field" -> fieldToJSON(gameState.field),
            "bombChance" -> gameState.bombChance,
            "width" -> gameState.width,
            "height" -> gameState.height,
            "firstMove" -> gameState.firstMove,
            "undoFields" -> Json.toJson(gameState.undoFields.map(fieldToJSON)),
            "redoFields" -> Json.toJson(gameState.redoFields.map(fieldToJSON)),
        )
    }

    private def gameStateFromJSON(json: JsValue): GameState = {
        val undos = (json \ "undos").as[Int]
        val maxUndos = (json \ "maxUndos").as[Int]
        val field = fieldFromJSON((json \ "field").get)
        val bombChance = (json \ "bombChance").as[Float]
        val width = (json \ "width").as[Int]
        val height = (json \ "height").as[Int]
        val firstMove = (json \ "firstMove").as[Boolean]
        val undoFields = (json \ "undoFields").as[JsArray].value.map(fieldFromJSON).toList
        val redoFields = (json \ "redoFields").as[JsArray].value.map(fieldFromJSON).toList
        GameState(undos, maxUndos, field, bombChance, width, height, firstMove, undoFields, redoFields)
    }

    override def load(path: String): Try[GameState] = Try {
        if FileExtension.get(path) != "json" then throw new IllegalArgumentException("File extension must be .json")
        val src = scala.io.Source.fromFile(path)
        val state = gameStateFromJSON(Json.parse(src.mkString))
        src.close()
        state
    }

    override def save(gameState: GameState, path: String): Try[Unit] = Try {
        if FileExtension.get(path) != "json" then throw new IllegalArgumentException("File extension must be .json")
        import java.io._
        val pw = new PrintWriter(new File(path))
        pw.write(Json.prettyPrint(gameStateToJSON(gameState)))
        pw.close()
    }
}