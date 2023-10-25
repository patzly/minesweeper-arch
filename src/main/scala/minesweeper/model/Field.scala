package minesweeper.model

class Field(rows: Int, cols: Int, genbomb: (Int, Int) => Cell) {
	val matrix = (0 until cols).map(y => 
		(0 until rows).map(x => genbomb(x, y)).toVector
	).toVector

	override def toString(): String = {
		matrix.map(r => r.mkString(" ")).mkString("\n")
	}
}