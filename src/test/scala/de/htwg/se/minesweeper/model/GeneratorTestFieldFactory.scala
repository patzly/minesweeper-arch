package de.htwg.se.minesweeper.model

import de.htwg.se.minesweeper.model._
import fieldComponent.FieldFactory
import fieldComponent.field._

class GeneratorTestFieldFactory(cell_generator: (Int, Int) => Cell) extends FieldFactory {
	override def createField(width: Int, height: Int, bombChance: Float): Field = Field(Vector.tabulate(width, height)(cell_generator))
}
