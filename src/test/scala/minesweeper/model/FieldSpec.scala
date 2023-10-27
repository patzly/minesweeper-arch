package minesweeper.model

import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class NRand(val result: Int) extends scala.util.Random {
    override def nextInt(n: Int): Int = result
}

class FieldSpec extends AnyWordSpec {
	"A Field" when {
		"it has 1 rows and columns" should {
			val fieldHidden = Field(1, 1, (x, y) => Cell(false, false))
			val fieldRevealed = Field(1, 1, (x, y) => Cell(true, false))
			val fieldBomb = Field(1, 1, (x, y) => Cell(true, true))

			"have a single Cell" in {
				fieldHidden.matrix.size shouldBe 1
				fieldHidden.matrix.head.size shouldBe 1
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
		}
		"it has 3 rows and columns and is empty" should {
			val fieldRevealed = Field(3, 3, (x, y) => Cell(true, false))
			"have a 3 columns and 3 rows" in {
				fieldRevealed.matrix.size shouldBe(3)
				fieldRevealed.matrix.head.size shouldBe(3)
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
		}
	}
	"Field.getRandBombGen" when {
        "used with a generator that always returns 0" should {
            val rand = new NRand(0)
            val genbomb = Field.getRandBombGen(rand, 0.25f)

            "always return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(true, true))
                genbomb(1, 1) shouldBe(Cell(true, true))
                genbomb(0, 1) shouldBe(Cell(true, true))
            }
        }
        "used with a generator that never returns 0" should {
            val rand = new NRand(1)
            val genbomb = Field.getRandBombGen(rand, 0.25f)

            "never return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(true, false))
                genbomb(1, 1) shouldBe(Cell(true, false))
                genbomb(0, 1) shouldBe(Cell(true, false))
            }
        }
    }
}