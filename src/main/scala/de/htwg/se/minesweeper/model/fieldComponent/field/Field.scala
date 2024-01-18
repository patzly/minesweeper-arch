package de.htwg.se.minesweeper.model.fieldComponent.field

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.model.Cell

type CellMatrix = Vector[Vector[Cell]]

// only implementation of FieldInterface
class Field(cellMatrix: CellMatrix) extends FieldInterface {
    // initialize the field with the number of nearby bombs
	private val matrix: CellMatrix = {
		cellMatrix.zipWithIndex.map((row, y) =>
			row.zipWithIndex.map((cell, x) =>
				cell.copy(nearbyBombs = countNearbyMinesImpl(x, y, cellMatrix))
			)
		)
	}

	// for the tests to work
	override def equals(other: Any): Boolean = {
		other match {
			case that: Field => matrix == that.matrix
			case _ => false
		}
	}

	override def toString: String = matrix.map(r => r.mkString(" ")).mkString("\n")
	
	override def getCell(x: Int, y: Int): Try[Cell] = Try(matrix(y)(x))

	override def getRow(row: Int): Try[Vector[Cell]] = Try(matrix(row))
	
	override def dimension: (Int, Int) = if matrix.isEmpty then (0,0) else (matrix(0).size, matrix.size)

	private def withCellRevealed(x: Int, y: Int, cellMatrix: CellMatrix): CellMatrix = {
		cellMatrix.updated(y, cellMatrix(y).updated(x, cellMatrix(y)(x).asRevealed))
	}

    
	override def withRevealed(x: Int, y: Int): Try[Field] = {
        // recursively reveal the cell at (x, y) and its neighbours if they are not bombs
		val newMatrix = Try(revealRec(x, y,
			withCellRevealed(x, y, matrix), // definitely reveal the clicked cell, wether it is a bomb or not
			Set.empty // at the beginning, no cells are revealed
		))
		newMatrix match {
			case Success((newMatrix, _)) => Success(Field(newMatrix))
			case Failure(exception) => Failure(exception)
		}
	}

	override def withToggledFlag(x: Int, y: Int) : Try[Field] = {
		Try(matrix.updated(y, matrix(y).updated(x, matrix(y)(x).asFlagToggled))) match {
			case Success(flagged) => Success(Field(flagged))
			case Failure(exception) => Failure(exception)
		}
	}

	private type IndexSet = Set[(Int, Int)]

    // recursive implementation of withRevealed
    // takes the x and y position of the current cell, a matrix of the current field and a set of cells that have already been revealed
	private def revealRec(xPos: Int, yPos: Int, matrix: CellMatrix, revealed: IndexSet): (CellMatrix, IndexSet) = {
        // if this cell is a bomb, do nothing
		if matrix(yPos)(xPos).isBomb then return (matrix, revealed)

        // reveal this cell and add it to the set of revealed cells
		val new_matrix = withCellRevealed(xPos, yPos, matrix)
		val new_revealed = revealed + ((xPos, yPos))

        // if this cell has nearby bombs, do nothing more
		if matrix(yPos)(xPos).nearbyBombs > 0 then return (new_matrix, new_revealed)

        // reveal_area is a 3x3 matrix of the cells around the clicked cell
		val reveal_area = new_matrix.zipWithIndex.map((row, y) =>
			row.zipWithIndex.map((col, x) =>
				(col, x, y)
			).slice(xPos-1, xPos+2)
		).slice(yPos-1, yPos+2)
		
        // filter out the cells that are already revealed or are bombs
		val filtered_area = reveal_area.flatten.filter((c, x, y) => !new_revealed.contains((x, y)) && !c.isBomb)
		
        // recursively reveal the filtered cells
		filtered_area.foldLeft((new_matrix, new_revealed))((acc, cell) => {
			val (x, y) = (cell._2, cell._3)
			revealRec(x, y, acc._1, acc._2)
		})
	}

    // returns the number of bombs in the 8 neighbouring cells
	private def countNearbyMinesImpl(x: Int, y: Int, matrix: CellMatrix): Int = {
		matrix.slice(y-1, y+2).transpose.slice(x-1, x+2).flatten.count(c => c.isBomb) - (if (matrix(y)(x).isBomb) 1 else 0)
	}

    // wrapper for countNearbyMinesImpl to catch index out of bounds exceptions
	override def countNearbyMines(x: Int, y: Int): Try[Int] = {
		Try(countNearbyMinesImpl(x, y, matrix))
	}

    // the player has won if all cells are revealed or are bombs
	override def hasWon: Boolean = {
		matrix.flatten.forall(cell => cell.isRevealed || cell.isBomb)
	}
}
