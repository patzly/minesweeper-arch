package de.htwg.se.minesweeper.model.fieldComponent.field

import de.htwg.se.minesweeper.model.fieldComponent._

class TestFieldFactory(matrix: CellMatrix) extends FieldFactory {
	override def createField(width: Int, height: Int, bomb_chande: Float): Field = Field(matrix)
}
