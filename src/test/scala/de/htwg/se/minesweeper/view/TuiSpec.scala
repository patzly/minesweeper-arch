package de.htwg.se.minesweeper.view

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.model.{Field, Cell}
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.view.Tui
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.model._

class TestObserver extends Observer[Event] with EventVisitor {
    var f: Field = null
    var w: WonEvent = null
    var l: LostEvent = null
    var e: ExitEvent = null
    override def update(ev: Event): Unit = {
        ev.accept(this)
    }

    override def visitSetup(event: SetupEvent): Unit = {
        f = event.field
    }

    override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
        f = event.field
    }

    override def visitWon(event: WonEvent): Unit = {
        w = event
    }

    override def visitLost(event: LostEvent): Unit = {
        l = event
    }

    override def visitExit(event: ExitEvent): Unit = {
        e = event
    }
}

class TuiSpec extends AnyWordSpec {
	"A Tui" when {
		"it has a single cell field" should {
			val controller = FieldController(TestFieldFactory(Vector(Vector(Cell(false, false)))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "                     \n   1\n   -\n1 |#"
				observer.f.toString shouldEqual("#")
				tui.processLine("abc 2")
				observer.f.toString shouldEqual("#")
				tui.processLine("2")
				observer.f.toString shouldEqual("#")
			}
			"after redoing an empty stack" in {
				tui.processLine("r")
				observer.f.toString shouldEqual("#")
			}
			"after flagging the cell" in {
				tui.processLine("1 1 flag")
				observer.f.toString shouldEqual("⚑")
				tui.processLine("2 2 flag")
				observer.f.toString shouldEqual("⚑")
			}
			"after undoing the flag" in {
				tui.processLine("u")
				observer.f.toString shouldEqual("#")
				tui.processLine("u")
				observer.f.toString shouldEqual("#")
			}
			"after undoing an empty stack" in {
				tui.processLine("u")
				observer.f.toString shouldEqual("#")
			}
			"after redoing the flag" in {
				tui.processLine("r")
				observer.f.toString shouldEqual("⚑")
			}
			"after redoing again" in {
				tui.processLine("r")
				observer.f.toString shouldEqual("⚑")
			}
			"after flagging the cell again" in {
				tui.processLine("1 1 flag")
				observer.f.toString shouldEqual("#")
			}
			"after revealing the cell" in {
				tui.processLine("1 1")
				observer.f.toString shouldEqual("☐")
				observer.w shouldBe(WonEvent())
				tui.processLine("2 2")
				observer.f.toString shouldEqual("☐")
			}
			"after quitting" in {
				tui.processLine("q")
				observer.e shouldBe(ExitEvent())
			}
		}
		"it has a multi cell field" should {
			val controller = FieldController(TestFieldFactory(Vector.tabulate(3, 3)((y, x) => Cell(false, x == 0))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2")
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc")
				observer.f.toString shouldEqual("# # #\n# # #\n# # #")
			}

			"after revealing some cells recursively" in {
				tui.processLine("3 3")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(observer.f) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				tui.processLine("4 4")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")

				tui.processLine("2 1")
				observer.f.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				
				tui.processLine("1 1")
				observer.f.toString shouldEqual("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.l shouldBe(LostEvent())
				tui.fieldString(observer.f) shouldEqual "                     \n   1 2 3\n   -----\n1 |☒ 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
			}
			"after quitting" in {
				tui.processLine("q")
				observer.e shouldBe(ExitEvent())
			}
		}
		"it is a long matrix" should {
			val controller = FieldController(TestFieldFactory(Vector.tabulate(1, 15)((y, x) => Cell(false, false))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"print correctly" in {
				controller.setup()
				tui.fieldString(observer.f) shouldEqual "                      1 1 1 1 1 1\n    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5\n    -----------------------------\n1  |# # # # # # # # # # # # # # #"
			}
		}
	}
}