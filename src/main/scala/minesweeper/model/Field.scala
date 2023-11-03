package minesweeper.model

import scala.util.Random

type CellMatrix = Vector[Vector[Cell]]

class Field(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
	private val matrix: CellMatrix = {
		val raw_matrix = Vector.tabulate(rows, cols)((y, x) => genbomb(x, y))
		raw_matrix.zipWithIndex.map((row, y) => 
			row.zipWithIndex.map((cell, x) => 
				cell.copy(nearbyBombs = countNearbyMinesImpl(x, y, raw_matrix))
			)
		)
	}

	override def toString: String = matrix.map(r => r.mkString(" ")).mkString("\n")
	
	def getCell(x: Int, y: Int): Cell = {
		check_out_of_bounds(x, y)
		matrix(y)(x)
	}

	private def revealCell(x: Int, y: Int, cellMatrix: CellMatrix): CellMatrix = {
		cellMatrix.updated(y, cellMatrix(y).updated(x, Cell(true, cellMatrix(y)(x).isBomb, cellMatrix(y)(x).nearbyBombs)))
	}

	def isInBounds(x: Int, y: Int): Boolean = {
		x >= 0 && y >= 0 && matrix.length > y && matrix(y).length > x
	}

	private def check_out_of_bounds(x: Int, y: Int): Unit = {
		if !isInBounds(x, y) then 
			throw new IndexOutOfBoundsException(s"Indices ($x, $y) out of bounds for field of dimension (${matrix.length}, ${matrix(0).length})")
	}

	def withRevealed(x: Int, y: Int): Field = {
		check_out_of_bounds(x, y)
		val newMatrix = revealRec(x, y,
			revealCell(x, y, matrix), // definitely reveal the clicked cell
			Set()
		)._1
		Field(rows, cols, (x: Int, y: Int) => newMatrix(y)(x))
	}

	private type IndexSet = Set[(Int, Int)]

	private def revealRec(xPos: Int, yPos: Int, matrix: CellMatrix, revealed: IndexSet): (CellMatrix, IndexSet) = {
		if matrix(yPos)(xPos).isBomb then return (matrix, revealed)

		val new_matrix = revealCell(xPos, yPos, matrix)
		val new_revealed = revealed + ((xPos, yPos))

		if matrix(yPos)(xPos).nearbyBombs > 0 then return (new_matrix, new_revealed)

		val to_reveal = new_matrix.zipWithIndex.map((row, y) =>
			row.zipWithIndex.map((col, x) =>
				(col, x, y)
			).slice(xPos-1, xPos+2)
		).slice(yPos-1, yPos+2)
		.flatten
		.filter((c, x, y) => !new_revealed.contains((x, y)) && !c.isBomb)

		to_reveal.foldLeft((new_matrix, new_revealed))((acc, cell) => {
			val (x, y) = (cell._2, cell._3)
			revealRec(x, y, acc._1, acc._2)
		})
	}

	private def countNearbyMinesImpl(x: Int, y: Int, matrix: CellMatrix) = {
		val sum = matrix.slice(y-1, y+2).map(row => row.slice(x-1, x+2).count(c => c.isBomb)).sum
		if matrix(y)(x).isBomb then sum - 1 else sum
	}

	def countNearbyMines(x: Int, y: Int): Int = {
		check_out_of_bounds(x, y)
		countNearbyMinesImpl(x, y, matrix)
	}

	def hasWon: Boolean = {
		matrix.flatten.forall(cell => cell.isRevealed || cell.isBomb)
	}
}

object Field {
	def getRandBombGen(rand: Random, bomb_chance: Float): (Int, Int) => Cell =
		(_, _) => Cell(false, rand.nextInt((1/bomb_chance).toInt) == 0)
}