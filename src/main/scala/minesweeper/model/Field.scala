package minesweeper.model

import scala.util.Random

class Field(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
	val matrix: Vector[Vector[Cell]] = Vector.tabulate(rows, cols) { (x, y) => genbomb(x, y)}

	override def toString: String = matrix.map(r => r.mkString(" ")).mkString("\n")
	
	private def isInBounds(x: Int, y: Int): Boolean = {
		matrix.length > y && matrix(y).length > x
	}

	private def check_out_of_bounds(x: Int, y: Int): Unit = {
		if !isInBounds(x, y) then 
			throw new IndexOutOfBoundsException(s"Indices ($x, $y) out of bounds for field of dimension (${matrix.length}, ${matrix(0).length})")
	}

	def withRevealed(x: Int, y: Int): Field = {
		check_out_of_bounds(x, y)
		val newMatrix = matrix.updated(y, matrix(y).updated(x, Cell(true, matrix(y)(x).isBomb)))
		Field(rows, cols, (x: Int, y: Int) => newMatrix(x)(y))
	}

	def countNearbyMines(x: Int, y: Int): Int = {
		check_out_of_bounds(x, y)
		val sum = matrix.slice(y-1, y+2).map(row => row.slice(x-1, x+2).count(c => c.isBomb)).sum
		if matrix(y)(x).isBomb then sum - 1 else sum
	}
}

object Field {
	def getRandBombGen(rand: Random, bomb_chance: Float): (Int, Int) => Cell =
		(_, _) => Cell(false, rand.nextInt((1/bomb_chance).toInt) == 0)
}