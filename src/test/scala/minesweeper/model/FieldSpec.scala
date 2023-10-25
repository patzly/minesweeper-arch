package minesweeper.model

import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class FieldSpec extends AnyWordSpec {
	"A Field" when {
		"it has 1 rows and columns" should {
			val fieldHidden = Field(1, 1, (x, y) => Cell(false, false))
			val fieldRevealed = Field(1, 1, (x, y) => Cell(true, false))
			val fieldBomb = Field(1, 1, (x, y) => Cell(true, true))

			"have a single hidden Cell" in {
				fieldHidden.toString shouldBe("#")
			}
			"have a single revealed empty Cell" in {
				fieldRevealed.toString shouldBe("☐")
			}
			"have a single revealed bomb Cell" in {
				fieldBomb.toString shouldBe("☒")
			}
		}
		"it has 3 rows and columns and is empty" should {
			val fieldRevealed = Field(3, 3, (x, y) => Cell(true, false))
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
}