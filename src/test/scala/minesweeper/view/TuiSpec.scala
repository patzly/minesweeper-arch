package minesweeper.view

import minesweeper.controller._
import minesweeper.view._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class TuiSpec extends AnyWordSpec {
	"A Tui" when {
		"it has a single cell field" should {
			val controller = FieldController(1, 1, (x, y) => Cell(false, false))
			val tui = Tui(controller)

			"without revealing the cell" in {
				tui.toString shouldEqual "   1\n   -\n1 |#"
				controller.field.toString shouldEqual("#")
				tui.processLine("abc 2") shouldEqual TUIState.Invalid("Invalid input: Please enter numbers!")
				controller.field.toString shouldEqual("#")
				tui.processLine("2") shouldEqual TUIState.Invalid("Invalid input: Format is <column> <row> ['flag']!")
				controller.field.toString shouldEqual("#")
			}
			"after flagging the cell" in {
				tui.processLine("1 1 flag") shouldEqual TUIState.Continue
				controller.field.toString shouldEqual "⚑"
				tui.processLine("2 2 flag") shouldEqual TUIState.Invalid("Indices (1, 1) out of bounds for field of dimension (1, 1)")
			}
			"after flagging the cell again" in {
				tui.processLine("1 1 flag") shouldEqual TUIState.Continue
				controller.field.toString shouldEqual "#"
			}
			"after revealing the cell" in {
				tui.processLine("1 1") shouldEqual TUIState.Won
				controller.field.toString shouldEqual("☐")
				controller.field.hasWon shouldBe(true)
				tui.processLine("2 2") shouldEqual TUIState.Invalid("Indices (1, 1) out of bounds for field of dimension (1, 1)")
				controller.field.toString shouldEqual("☐")
			}
			"after quitting" in {
				tui.processLine("q") shouldEqual TUIState.Exit
			}
		}
		"it has a multi cell field" should {
			val controller = FieldController(3, 3, (x, y) => Cell(false, x == 0))
			val tui = Tui(controller)
			"without revealing the cell" in {
				tui.toString shouldEqual "   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2") shouldEqual TUIState.Invalid("Invalid input: Please enter numbers!")
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc") shouldEqual TUIState.Invalid("Invalid input: Format is <column> <row> ['flag']!")
				controller.field.toString shouldEqual("# # #\n# # #\n# # #")
			}
			"after revealing some cells recursively" in {
				tui.processLine("3 3") shouldEqual TUIState.Won
				controller.field.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
				tui.processLine("4 4") shouldEqual TUIState.Invalid("Indices (3, 3) out of bounds for field of dimension (3, 3)")
				controller.field.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
				tui.processLine("2 1") shouldEqual TUIState.Invalid("Cell already revealed!")
				controller.field.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
				tui.processLine("1 1") shouldEqual TUIState.Lost
				controller.field.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
				controller.field.hasWon shouldBe(true)
			}
			"after quitting" in {
				tui.processLine("q") shouldEqual TUIState.Exit
			}
		}
		"it has a field with bombs" should {
			val controller = FieldController(3, 3, (x, y) => Cell(false, x == 0))
			val tui = Tui(controller)
			"return false if the game is lost or won" in {
				tui.processLine("3 3") shouldEqual TUIState.Won
				controller.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				controller.field.hasWon shouldBe(true)
				tui.processLine("1 1") shouldEqual TUIState.Lost
				controller.field.toString shouldEqual("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
			}
		}
	}
}