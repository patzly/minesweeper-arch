package minesweeper.model

case class Cell(isRevealed: Boolean, isBomb: Boolean):
	override def toString: String = {
		val bombChar = "☒"
		val revealedChar = "☐"
		val hiddenChar = "#"

		if isRevealed then
			if isBomb then bombChar else revealedChar
			else hiddenChar
	}