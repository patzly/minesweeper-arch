import scala.util.Random

val rand = new Random()

@main def main: Unit =
	val width = 8
	val height = 8
	val bomb_chance = 0.25f
	val bombs = gen_bombs(width, height, bomb_chance, rand)

	print(get_field(width, height, bombs))

// returns a string representation of the field
def get_field(width: Int, height: Int, bombs: List[(Int, Int)]): String = 
	// constants for the field
	val hidden   = "#"
	val revealed = " "
	val bomb     = "o"

	// generate the field as a string
	(0 until height).map(y => 
		(0 until width).map(x => 
			(if bombs.contains((x, y)) 
				then bomb 
				else hidden
			) + " ")
		.toArray.mkString + "\n"
	).toArray.mkString

// generates a list of bomb coordinates with a given percentage
def gen_bombs(width: Int, height: Int, percent: Float, rng: Random): List[(Int, Int)] =
	(for {
		x <- 0 until width
		y <- 0 until height
		if rng.nextInt((1/percent).toInt) == 0
	} yield (x, y)).toList
