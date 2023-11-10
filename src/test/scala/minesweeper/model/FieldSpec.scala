package minesweeper.model

import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import scala.util.Success
import scala.util.Failure

class NRand(val result: Int) extends scala.util.Random {
    override def nextInt(n: Int): Int = result
}

class FieldSpec extends AnyWordSpec {
	"A Field" when {
		"it has 1 rows and columns" should {
			val fieldHidden = Field(1, 1, (x, y) => Cell(false, false))
			val fieldRevealed = Field(1, 1, (x, y) => Cell(true, false))
			val fieldBomb = Field(1, 1, (x, y) => Cell(true, true))

			"check bounds correctly" in {
				fieldHidden.isInBounds(0, 0) shouldBe(true)
				fieldHidden.isInBounds(1, 0) shouldBe(false)
				fieldHidden.isInBounds(0, 1) shouldBe(false)
				fieldHidden.isInBounds(1, 1) shouldBe(false)
				fieldHidden.isInBounds(-1, 0) shouldBe(false)
				fieldHidden.isInBounds(0, -1) shouldBe(false)
			}

			"print a single hidden Cell" in {
				fieldHidden.toString shouldBe("#")
			}
			"print a single revealed empty Cell" in {
				fieldRevealed.toString shouldBe("☐")
			}
			"print a single revealed bomb Cell" in {
				fieldBomb.toString shouldBe("☒")
			}
			"return the correct number of nearby bombs" in {
				fieldHidden.countNearbyMines(0, 0) shouldBe(Success(0))
				fieldRevealed.countNearbyMines(0, 0) shouldBe(Success(0))
				fieldBomb.countNearbyMines(0, 0) shouldBe(Success(0))
			}
			"return the correct number of nearby bombs for out of bounds indices" in {
				fieldHidden.countNearbyMines(0, 0) shouldBe(Success(0))
				fieldRevealed.countNearbyMines(0, 0) shouldBe(Success(0))
				fieldBomb.countNearbyMines(0, 0) shouldBe(Success(0))
			}
			"check correctly if the game is won" in {
				fieldHidden.hasWon shouldBe(false)
				fieldRevealed.hasWon shouldBe(true)
				fieldBomb.hasWon shouldBe(true)
			}
		}
		"it has 3 rows and columns and is empty" should {
			val fieldRevealed = Field(3, 3, (x, y) => Cell(true, false))

			"check bounds correctly" in {
				fieldRevealed.isInBounds(0, 0) shouldBe(true)
				fieldRevealed.isInBounds(1, 0) shouldBe(true)
				fieldRevealed.isInBounds(2, 0) shouldBe(true)
				fieldRevealed.isInBounds(0, 1) shouldBe(true)
				fieldRevealed.isInBounds(1, 1) shouldBe(true)
				fieldRevealed.isInBounds(2, 1) shouldBe(true)
				fieldRevealed.isInBounds(0, 2) shouldBe(true)
				fieldRevealed.isInBounds(1, 2) shouldBe(true)
				fieldRevealed.isInBounds(2, 2) shouldBe(true)
				fieldRevealed.isInBounds(3, 0) shouldBe(false)
				fieldRevealed.isInBounds(0, 3) shouldBe(false)
				fieldRevealed.isInBounds(3, 3) shouldBe(false)
				fieldRevealed.isInBounds(-1, 0) shouldBe(false)
				fieldRevealed.isInBounds(0, -1) shouldBe(false)
			}

			"be printed correctly if all Cells are revealed" in {
				fieldRevealed.toString shouldBe("☐ ☐ ☐\n☐ ☐ ☐\n☐ ☐ ☐")
			}

			val fieldHidden = Field(3, 3, (x, y) => Cell(false, false))
			"be printed correctly if all Cells are hidden" in {
				fieldHidden.toString shouldBe("# # #\n# # #\n# # #")
			}

			val fieldBomb = Field(3, 3, (x, y) => Cell(true, true))
			"be printed correctly if all Cells are bombs" in {
				fieldBomb.toString shouldBe("☒ ☒ ☒\n☒ ☒ ☒\n☒ ☒ ☒")
			}

			"check correctly if the game is won" in {
				fieldHidden.hasWon shouldBe(false)
				fieldRevealed.hasWon shouldBe(true)
				fieldBomb.hasWon shouldBe(true)
			}
		}
		"it has 3 ros and colums and bombs on the diagonal" should {
			val fieldWithBombs = Field(3, 3, (x, y) => Cell(false, x == y))
			"return the correct bomb count for each cell" in {
				fieldWithBombs.countNearbyMines(0, 0) shouldBe(Success(1))
				fieldWithBombs.countNearbyMines(1, 0) shouldBe(Success(2))
				fieldWithBombs.countNearbyMines(2, 0) shouldBe(Success(1))

				fieldWithBombs.countNearbyMines(0, 1) shouldBe(Success(2))
				fieldWithBombs.countNearbyMines(1, 1) shouldBe(Success(2))
				fieldWithBombs.countNearbyMines(2, 1) shouldBe(Success(2))
				
				fieldWithBombs.countNearbyMines(0, 2) shouldBe(Success(1))
				fieldWithBombs.countNearbyMines(1, 2) shouldBe(Success(2))
				fieldWithBombs.countNearbyMines(2, 2) shouldBe(Success(1))
			}
			"check correctly if the game is won" in {
				fieldWithBombs.hasWon shouldBe(false)
			}
		}
	}
	"Field.getRandBombGen" when {
        "used with a generator that always returns 0" should {
            val rand = new NRand(0)
            val genbomb = Field.getRandBombGen(rand, 0.25f)

            "always return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(false, true))
                genbomb(1, 1) shouldBe(Cell(false, true))
                genbomb(0, 1) shouldBe(Cell(false, true))
            }
        }
        "used with a generator that never returns 0" should {
            val rand = new NRand(1)
            val genbomb = Field.getRandBombGen(rand, 0.25f)

            "never return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(false, false))
                genbomb(1, 1) shouldBe(Cell(false, false))
                genbomb(0, 1) shouldBe(Cell(false, false))
            }
        }
    }
}