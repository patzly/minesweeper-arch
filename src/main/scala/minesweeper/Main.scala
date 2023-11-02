package minesweeper

import scala.util.Random
import model.*
import view.Tui
import controller.FieldController

@main def main: Unit =
	val width = 8
	val height = 8
	val bomb_chance = 0.25f
	val rand = new Random()

	val controller = FieldController(height, width, Field.getRandBombGen(rand, bomb_chance))
	val tui = Tui(controller)
	println(controller.field)
	while tui.processLine(scala.io.StdIn.readLine()) do
		{}

