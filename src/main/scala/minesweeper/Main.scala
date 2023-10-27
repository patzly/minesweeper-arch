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
	val field: Field = Field(width, height, Field.getRandBombGen(rand, bomb_chance))

	println(field)
	val controller = FieldController(field)
	val tui = Tui(controller)
	while tui.processLine(scala.io.StdIn.readLine()) do
		{}

