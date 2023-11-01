package minesweeper.view

import minesweeper.controller._
import minesweeper.view._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException

class TuiSpec extends AnyWordSpec {
	"A Tui" when {
		"it has a single cell field" should {
			val field = Field(1, 1, (x, y) => Cell(false, false))
			val controller = FieldController(field)
			val tui = Tui(controller)

			"without revealing the cell" in {
				controller.field.toString shouldEqual("#")
				tui.processLine("abc 2") shouldEqual(true)
				controller.field.toString shouldEqual("#")
				tui.processLine("2") shouldEqual(true)
				controller.field.toString shouldEqual("#")
			}
			"after revealing the cell" in {
				tui.processLine("1 1") shouldEqual(false)
				controller.field.toString shouldEqual("☐")
				controller.field.hasWon shouldBe(true)
				tui.processLine("2 2") shouldEqual(true)
				controller.field.toString shouldEqual("☐")
			}
			"after quitting" in {
				tui.processLine("q") shouldEqual(false)
			}
		}
		"it has a multi cell field" should {
			val field = Field(3, 3, (x, y) => Cell(false, false))
			val controller = FieldController(field)
			val tui = Tui(controller)
			"without revealing the cell" in {
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2") shouldEqual(true)
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc") shouldEqual(true)
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
			}
			"after revealing some cells" in {
				tui.processLine("1 1") shouldEqual(true)
				controller.field.toString shouldBe("☐ # #\n# # #\n# # #")
				tui.processLine("3 3") shouldEqual(true)
				controller.field.toString shouldBe("☐ # #\n# # #\n# # ☐")
				tui.processLine("4 4") shouldEqual(true)
				controller.field.toString shouldBe("☐ # #\n# # #\n# # ☐")
				controller.field.hasWon shouldBe(false)
			}
			"after quitting" in {
				tui.processLine("q") shouldEqual(false)
			}
		}
		"it has a field with bombs" should {
			val field = Field(1, 1, (x, y) => Cell(false, true))
			val controller = FieldController(field)
			val tui = Tui(controller)
			"return false if the game is lost" in {
				tui.processLine("1 1") shouldEqual(false)
				controller.field.toString shouldEqual("☒")
				controller.field.hasWon shouldBe(true)
			}
		}
	}
}