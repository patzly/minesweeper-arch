package de.htwg.se.minesweeper.model.fieldComponent.field

import de.htwg.se.minesweeper.model.fieldComponent._

class TestFieldFactory(matrix: CellMatrix) extends FieldFactory {
	override def createField(): Field = Field(matrix)
}
