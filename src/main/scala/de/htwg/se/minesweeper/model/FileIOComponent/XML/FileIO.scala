package de.htwg.se.minesweeper.model.FileIOComponent.XML

import de.htwg.se.minesweeper.model.FileIOComponent.FileIOInterface
import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import scala.util.{Try, Success, Failure}
import de.htwg.se.minesweeper.model.FileIOComponent.FileExtension

class FileIO extends FileIOInterface {
    private def cellToXML(cell: Cell): scala.xml.Node = {
        <cell>
            <isRevealed>{cell.isRevealed}</isRevealed>
            <isBomb>{cell.isBomb}</isBomb>
            <isFlagged>{cell.isFlagged}</isFlagged>
            <nearbyBombs>{cell.nearbyBombs}</nearbyBombs>
        </cell>
    }
    private def cellFromXML(node: scala.xml.Node): Cell = {
        val isRevealed = (node \ "isRevealed").text.toBoolean
        val isBomb = (node \ "isBomb").text.toBoolean
        val isFlagged = (node \ "isFlagged").text.toBoolean
        val nearbyBombs = (node \ "nearbyBombs").text.toInt
        Cell(isRevealed, isBomb, isFlagged, nearbyBombs)
    }

    private def fieldToXML(field: FieldInterface): scala.xml.Node = {
        <field>
            <matrix>
                {
                    for {
                        y <- 0 until field.dimension._2
                    } yield {
                        <row>
                            {
                                for {
                                    x <- 0 until field.dimension._1
                                } yield {
                                    cellToXML(field.getCell(x, y).get)
                                }
                            }
                        </row>
                    }
                }
            </matrix>
        </field>
    }
    private def fieldFromXML(node: scala.xml.Node): FieldInterface = {
        val matrix = (node \ "matrix").head
        val rows = (matrix \ "row")
        val cells = rows.map(row => (row \ "cell").map(cellFromXML))
        val cellMatrix = cells.map(_.toVector).toVector
        FieldInterface.fromMatrix(cellMatrix)
    }

    private def gameStateToXML(gameState: GameState): scala.xml.Node = {
        <gameState>
            <undos>{gameState.undos}</undos>
            <maxUndos>{gameState.maxUndos}</maxUndos>
            {fieldToXML(gameState.field)}
            <bombChance>{gameState.bombChance}</bombChance>
            <width>{gameState.width}</width>
            <height>{gameState.height}</height>
            <firstMove>{gameState.firstMove}</firstMove>
            <undoFields>
                {
                    for {
                        field <- gameState.undoFields
                    } yield fieldToXML(field)
                }
            </undoFields>
            <redoFields>
                {
                    for {
                        field <- gameState.redoFields
                    } yield fieldToXML(field)
                }
            </redoFields>
        </gameState>
    }
    private def gameStateFromXML(node: scala.xml.Node): GameState = {
        val undos = (node \ "undos").text.toInt
        val maxUndos = (node \ "maxUndos").text.toInt
        val field = fieldFromXML((node \ "field").head)
        val bombChance = (node \ "bombChance").text.toFloat
        val width = (node \ "width").text.toInt
        val height = (node \ "height").text.toInt
        val firstMove = (node \ "firstMove").text.toBoolean
        val undoFields = (node \ "undoFields" \ "field").map(fieldFromXML).toList
        val redoFields = (node \ "redoFields" \ "field").map(fieldFromXML).toList
        GameState(undos, maxUndos, field, bombChance, width, height, firstMove, undoFields, redoFields)
    }

    override def save(gameState: GameState, path: String): Try[Unit] = Try {
        if FileExtension.get(path) != "xml" then throw new IllegalArgumentException("File extension must be .xml")
        scala.xml.XML.save(path, gameStateToXML(gameState))
    }
    override def load(path: String): Try[GameState] = Try {
        if FileExtension.get(path) != "xml" then throw new IllegalArgumentException("File extension must be .xml")
        val node = scala.xml.XML.loadFile(path)
        gameStateFromXML(node)
    }
}