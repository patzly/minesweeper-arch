package minesweeper.controller

import scala.util.Random
import minesweeper.model.Cell

def genbombRand(rand: Random, bomb_chance: Float): (Int, Int) => Cell =
	(_, _) => Cell(true, rand.nextInt((1/bomb_chance).toInt) == 0)