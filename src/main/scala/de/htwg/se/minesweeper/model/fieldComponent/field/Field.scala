package de.htwg.se.minesweeper.model.fieldComponent.field

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.model.Cell
import scala.xml.NodeSeq
import scala.xml.Node

type CellMatrix = Vector[Vector[Cell]]

class Field(cellMatrix: CellMatrix) extends FieldInterface {
	private val matrix: CellMatrix = {
		cellMatrix.zipWithIndex.map((row, y) =>
			row.zipWithIndex.map((cell, x) =>
				cell.copy(nearbyBombs = countNearbyMinesImpl(x, y, cellMatrix))
			)
		)
	}

	// for the tests
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

	private def revealCell(x: Int, y: Int, cellMatrix: CellMatrix): CellMatrix = {
		cellMatrix.updated(y, cellMatrix(y).updated(x, cellMatrix(y)(x).asRevealed))
	}

	override def withRevealed(x: Int, y: Int): Try[Field] = {
		val newMatrix = Try(revealRec(x, y,
			revealCell(x, y, matrix), // definitely reveal the clicked cell
			Set.empty
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

	private def revealRec(xPos: Int, yPos: Int, matrix: CellMatrix, revealed: IndexSet): (CellMatrix, IndexSet) = {
		if matrix(yPos)(xPos).isBomb then return (matrix, revealed)

		val new_matrix = revealCell(xPos, yPos, matrix)
		val new_revealed = revealed + ((xPos, yPos))

		if matrix(yPos)(xPos).nearbyBombs > 0 then return (new_matrix, new_revealed)

		val reveal_area = new_matrix.zipWithIndex.map((row, y) =>
			row.zipWithIndex.map((col, x) =>
				(col, x, y)
			).slice(xPos-1, xPos+2)
		).slice(yPos-1, yPos+2)
		
		val filtered_area = reveal_area.flatten.filter((c, x, y) => !new_revealed.contains((x, y)) && !c.isBomb)
		
		filtered_area.foldLeft((new_matrix, new_revealed))((acc, cell) => {
			val (x, y) = (cell._2, cell._3)
			revealRec(x, y, acc._1, acc._2)
		})
	}

	private def countNearbyMinesImpl(x: Int, y: Int, matrix: CellMatrix): Int = {
		matrix.slice(y-1, y+2).transpose.slice(x-1, x+2).flatten.count(c => c.isBomb) - (if (matrix(y)(x).isBomb) 1 else 0)
	}

	override def countNearbyMines(x: Int, y: Int): Try[Int] = {
		Try(countNearbyMinesImpl(x, y, matrix))
	}

	override def hasWon: Boolean = {
		matrix.flatten.forall(cell => cell.isRevealed || cell.isBomb)
	}
}