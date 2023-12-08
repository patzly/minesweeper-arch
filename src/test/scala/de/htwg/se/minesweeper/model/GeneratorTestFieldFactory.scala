package de.htwg.se.minesweeper.model

import de.htwg.se.minesweeper.model._
import fieldComponent.FieldFactory
import fieldComponent.field._

class GeneratorTestFieldFactory(rows: Int, cols: Int, cell_generator: (Int, Int) => Cell) extends FieldFactory {
	override def createField(): Field = Field(Vector.tabulate(cols, rows)(cell_generator))
}
