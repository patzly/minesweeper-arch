package minesweeper.model

import java.util.Random

class Field(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
	val matrix = Vector.tabulate(rows, cols) {(x, y) => genbomb(x, y)}

	override def toString(): String = {
		matrix.map(r => r.mkString(" ")).mkString("\n")
	}
}