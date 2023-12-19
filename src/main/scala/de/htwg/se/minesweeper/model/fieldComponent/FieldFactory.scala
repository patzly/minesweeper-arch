package de.htwg.se.minesweeper.model.fieldComponent

trait FieldFactory {
	def createField(width: Int, height: Int, bomb_chance: Float): FieldInterface;
}
