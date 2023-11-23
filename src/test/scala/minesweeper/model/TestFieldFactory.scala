package minesweeper.model

class TestFieldFactory(matrix: CellMatrix) extends FieldFactory {
	override def createField(): Field = Field(matrix)
}
