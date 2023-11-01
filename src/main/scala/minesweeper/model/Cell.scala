package minesweeper.model

case class Cell(isRevealed: Boolean, isBomb: Boolean, nearbyBombs: Int = 0):
	override def toString: String = {
		val bombChar = "☒"
		val revealedChar = "☐"
		val hiddenChar = "#"

		if isRevealed then
			if isBomb then bombChar 
			else if nearbyBombs == 0 then revealedChar else nearbyBombs.toString
		else hiddenChar
	}