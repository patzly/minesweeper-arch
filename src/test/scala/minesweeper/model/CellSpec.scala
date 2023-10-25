package minesweeper.model

import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class CellSpec extends AnyWordSpec {
	"A Cell" when {
		"it's a bomb and revealed" should {
			val cell = Cell(true, true)

			"be printed as the bomb char" in {
				cell.toString() should be("☒")
			}
		}

		"it's a bomb and hidden" should {
			val cell = Cell(false, true)

			"be printed as the hidden char" in {
				cell.toString() should be("#")
			}
		}

		"it's a not bomb and revealed" should {
			val cell = Cell(true, false)

			"be printed as the revealed char" in {
				cell.toString() should be("☐")
			}
		}

		"it's a not bomb and hidden" should {
			val cell = Cell(false, false)

			"be printed as the hidden char" in {
				cell.toString() should be("#")
			}
		}
	}
}