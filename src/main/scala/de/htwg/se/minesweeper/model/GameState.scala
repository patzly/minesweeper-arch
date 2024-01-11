package de.htwg.se.minesweeper.model

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import scala.util.{Try, Success, Failure}

// cannot be a case class to enable the circular reference in redoState
case class GameState(
    undos: Int,
    maxUndos: Int, 
    field: FieldInterface, 
    bombChance: Float, 
    width: Int, 
    height: Int,
    firstMove: Boolean = true,
    undoFields: List[FieldInterface] = List.empty,
    redoFields: List[FieldInterface] = List.empty, // has to be var for the circular reference
) {
    def cantRedo: Boolean = redoFields.isEmpty
    def cantUndo: Boolean = undos <= 0 || undoFields.isEmpty
    def undo: Try[GameState] = {
        if cantUndo then 
            Failure(new IllegalStateException("Cannot undo"))
        else 
            Success(copy(
                undos = undos - 1,
                field = undoFields.head,
                undoFields = undoFields.tail,
                redoFields = this.field :: redoFields,
            ))
    }
    def redo: Try[GameState] = {
        if cantRedo then 
            Failure(new IllegalStateException("Cannot redo"))
        else 
            Success(copy(
                field = redoFields.head,
                undoFields = this.field :: undoFields,
                redoFields = redoFields.tail,
            ))
    }
    def updateField(newField: FieldInterface): GameState = {
        copy(
            field = newField,
            firstMove = false,
            undoFields = this.field :: undoFields,
            redoFields = List.empty,
        )
    }
}