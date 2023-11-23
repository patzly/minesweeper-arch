package minesweeper.model

class GeneratorTestFieldFactory(rows: Int, cols: Int, cell_generator: (Int, Int) => Cell) extends FieldFactory {
	override def createField(): Field = Field(Vector.tabulate(cols, rows)(cell_generator))
}
