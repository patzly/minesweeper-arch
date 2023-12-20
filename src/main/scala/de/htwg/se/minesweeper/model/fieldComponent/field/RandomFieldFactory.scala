package de.htwg.se.minesweeper.model.fieldComponent.field

import scala.util.Random
import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.model.fieldComponent.FieldFactory

class RandomFieldFactory(rand: Random) extends FieldFactory {
	override def createField(width: Int, height: Int, bomb_chance: Float): Field = {
		Field(Vector.tabulate(height, width)((y, x) => Cell(false, rand.nextInt((1/bomb_chance).toInt) == 0)))
	}
}
