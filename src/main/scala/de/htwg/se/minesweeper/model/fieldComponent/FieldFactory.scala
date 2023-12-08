package de.htwg.se.minesweeper.model.fieldComponent

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface

trait FieldFactory {
	def createField(): FieldInterface;
}
