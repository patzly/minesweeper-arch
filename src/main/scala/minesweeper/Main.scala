package minesweeper

import scala.util.Random
import model.*

@main def main: Unit =
	val width = 8
	val height = 8
	val bomb_chance = 0.25f
	val rand = new Random()
	val field: Field = Field(width, height, Field.getRandBombGen(rand, bomb_chance))

	print(field)

