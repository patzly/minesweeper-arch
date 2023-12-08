package de.htwg.se.minesweeper.model.fieldComponent.field

import scala.util.Random
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.model.fieldComponent.FieldFactory

class RandomFieldFactory(rows: Int, cols: Int, rand: Random, bomb_chance: Float) extends FieldFactory {
	override def createField(): Field = {
		Field(Vector.tabulate(rows, cols)((y, x) => Cell(false, rand.nextInt((1/bomb_chance).toInt) == 0)))
	}
}
