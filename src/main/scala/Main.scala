import scala.util.Random
import model.*

val rand = new Random()

@main def main: Unit =
	val width = 8
	val height = 8
	val bomb_chance = 0.25f
	val field = Field(width, height, (x, y) => Cell(true, rand.nextInt((1/bomb_chance).toInt) == 0))

	print(field)

