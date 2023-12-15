package de.htwg.se.minesweeper.model.fieldComponent

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface

trait FieldFactory {
	def createField(width: Int, height: Int, bomb_chance: Float): FieldInterface;
}
