package de.htwg.se.minesweeper.model

import de.htwg.se.minesweeper.model.{CellMatrix, Field, FieldFactory}

class TestFieldFactory(matrix: CellMatrix) extends FieldFactory {
	override def createField(): Field = Field(matrix)
}
