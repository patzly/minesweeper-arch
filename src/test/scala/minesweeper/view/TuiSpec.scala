package minesweeper.view

import minesweeper.controller._
import minesweeper.view._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import minesweeper.observer.Observer

class TestObserver extends Observer[Event] {
	var f: Field = null
	var w: Event = null
	var l: Event = null
	var e: Event = null
	override def update(ev: Event): Unit = {
		ev match {
			case Event.Setup(field) => f = field
			case Event.FieldUpdated(field) => f = field
			case Event.Won => w = Event.Won
			case Event.Lost => l = Event.Lost
			case Event.Exit => e = Event.Exit
			case _ => ()
		}
	}
}

class TuiSpec extends AnyWordSpec {
	"A Tui" when {
		"it has a single cell field" should {
			val controller = FieldController(1, 1, (x, y) => Cell(false, false))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "   1\n   -\n1 |#"
				observer.f.toString shouldEqual("#")
				tui.processLine("abc 2")
				observer.f.toString shouldEqual("#")
				tui.processLine("2")
				observer.f.toString shouldEqual("#")
			}
			"after flagging the cell" in {
				tui.processLine("1 1 flag")
				observer.f.toString shouldEqual("⚑")
				tui.processLine("2 2 flag")
				observer.f.toString shouldEqual("⚑")
			}
			"after flagging the cell again" in {
				tui.processLine("1 1 flag")
				observer.f.toString shouldEqual("#")
			}
			"after revealing the cell" in {
				tui.processLine("1 1")
				observer.f.toString shouldEqual("☐")
				observer.w shouldBe(Event.Won)
				tui.processLine("2 2")
				observer.f.toString shouldEqual("☐")
			}
			"after quitting" in {
				tui.processLine("q")
				observer.e shouldBe(Event.Exit)
			}
		}
		"it has a multi cell field" should {
			val controller = FieldController(3, 3, (x, y) => Cell(false, x == 0))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2")
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc")
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
			}

			"after revealing some cells recursively" in {
				tui.processLine("3 3")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(Event.Won)
				tui.fieldString(observer.f) shouldEqual "   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				tui.processLine("4 4")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")

				tui.processLine("2 1")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				
				tui.processLine("1 1")
				observer.f.toString shouldEqual("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.l shouldBe(Event.Lost)
				tui.fieldString(observer.f) shouldEqual "   1 2 3\n   -----\n1 |☒ 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
			}
			"after quitting" in {
				tui.processLine("q")
				observer.e shouldBe(Event.Exit)
			}
		}
		"it is a long matrix" should {
			val controller = FieldController(1, 15, (x, y) => Cell(false, false))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"print correctly" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5\n    -----------------------------\n1  |# # # # # # # # # # # # # # #"
			}
		}
	}
}