import scala.util.Random

val rand = new Random()

@main def main: Unit =
  print(get_field(8, 8))

def get_field(width: Int, height: Int): String = 
	val hidden   = "X"
	val revealed = "▯"
	val bomb     = "B"

	def get_random_field(percent: Float): String = 
		rand.nextInt((1/percent).toInt) match
		case 0 => bomb
		case _ => hidden

	(0 until height).map(
		_ => (0 until width).map(_ => get_random_field(0.25) + " ")
		.toArray.mkString + "\n"
	).toArray.mkString

