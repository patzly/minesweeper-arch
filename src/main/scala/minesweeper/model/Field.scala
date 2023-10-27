package minesweeper.model

import scala.util.Random

class Field(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
	val matrix = Vector.tabulate(rows, cols) {(x, y) => genbomb(x, y)}

	override def toString(): String = {
		matrix.map(r => r.mkString(" ")).mkString("\n")
	}
}

object Field {
	def getRandBombGen(rand: Random, bomb_chance: Float): (Int, Int) => Cell =
		(_, _) => Cell(true, rand.nextInt((1/bomb_chance).toInt) == 0)
}