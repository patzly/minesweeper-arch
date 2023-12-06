package de.htwg.se.minesweeper.model

import scala.util.Random

class RandomFieldFactory(rows: Int, cols: Int, rand: Random, bomb_chance: Float) extends FieldFactory {
	override def createField(): Field = {
		Field(Vector.tabulate(rows, cols)((y, x) => Cell(false, rand.nextInt((1/bomb_chance).toInt) == 0)))
	}
}
