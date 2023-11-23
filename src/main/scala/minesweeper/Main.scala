package minesweeper

import scala.util.Random
import model.*
import view.Tui
import controller.FieldController

@main def main(): Unit = {
	val width = 16
	val height = 16
	val bomb_chance = 0.15f
	val rand = Random()

	val controller = FieldController(RandomFieldFactory(height, width, rand, bomb_chance))
	val tui = Tui(controller)

	controller.setup()
	tui.play()
}