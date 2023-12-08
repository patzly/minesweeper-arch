package de.htwg.se.minesweeper.view

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.view.Tui
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.spieController.SpieController
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.model.fieldComponent.field._

class TestObserver extends Observer[Event] with EventVisitor {
	var w: WonEvent = null
	var l: LostEvent = null
	override def update(ev: Event): Unit = {
		ev.accept(this)
	}

	override def visitExit(event: ExitEvent): Unit = {}
	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {}
	override def visitSetup(event: SetupEvent): Unit = {}

	override def visitWon(event: WonEvent): Unit = {
		w = event
	}

	override def visitLost(event: LostEvent): Unit = {
		l = event
	}
}

class TuiSpec extends AnyWordSpec {
	"A Tui" when {
		"it has a single cell field" should {
			val controller = SpieController(1, TestFieldFactory(Vector(Vector(Cell(false, false)))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(controller.getField) shouldEqual "                     \n   1\n   -\n1 |#"
				controller.getField.toString shouldEqual("#")
				tui.processLine("abc 2")
				controller.getField.toString shouldEqual("#")
				tui.processLine("2")
				controller.getField.toString shouldEqual("#")
			}
			"after redoing an empty stack" in {
				tui.processLine("r")
				controller.getField.toString shouldEqual("#")
			}
			"after flagging the cell" in {
				tui.processLine("1 1 flag")
				controller.getField.toString shouldEqual("⚑")
				tui.processLine("2 2 flag")
				controller.getField.toString shouldEqual("⚑")
			}
			"after undoing the flag" in {
				tui.processLine("u")
				controller.getField.toString shouldEqual("#")
				tui.processLine("u")
				controller.getField.toString shouldEqual("#")
			}
			"after undoing an empty stack" in {
				tui.processLine("u")
				controller.getField.toString shouldEqual("#")
			}
			"after redoing the flag" in {
				tui.processLine("r")
				controller.getField.toString shouldEqual("⚑")
			}
			"after redoing again" in {
				tui.processLine("r")
				controller.getField.toString shouldEqual("⚑")
			}
			"after flagging the cell again" in {
				tui.processLine("1 1 flag")
				controller.getField.toString shouldEqual("#")
			}
			"after revealing the cell" in {
				tui.processLine("1 1")
				controller.getField.toString shouldEqual("☐")
				observer.w shouldBe(WonEvent())
				tui.processLine("2 2")
				controller.getField.toString shouldEqual("☐")
			}
			"after quitting" in {
				tui.processLine("q")
				controller.didExit shouldBe(true)
			}
		}
		"it has a multi cell field" should {
			val controller = SpieController(1, TestFieldFactory(Vector.tabulate(3, 3)((y, x) => Cell(false, x == 0))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
			}

			"after revealing some cells recursively" in {
				tui.processLine("3 3")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				tui.processLine("4 4")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")

				tui.processLine("2 1")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				observer.w shouldBe(WonEvent())
			}
			"after winning and retrying" in {
				tui.processLine("y")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"when winning" in {
				tui.processLine("3 3")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
			}
			"after retrying" in {
				tui.processLine("y")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"when retrying without having lost or won" in {
				tui.processLine("y")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				tui.processLine("n")
				controller.getField.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"after winning and not retrying" in {
				tui.processLine("3 3")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
				tui.processLine("n")
				controller.didExit shouldBe(true)
			}
			"after winning and quitting" in {
				controller.setup()
				tui.processLine("3 3")
				controller.getField.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getField) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
				tui.processLine("q")
				controller.didExit shouldBe(true)
			}
			"after quitting" in {
				controller.setup()
				tui.processLine("q")
				controller.didExit shouldBe(true)
			}
		}
		"it is a long matrix" should {
			val controller = SpieController(1, TestFieldFactory(Vector.tabulate(1, 15)((y, x) => Cell(false, x == 2))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"print correctly" in {
				controller.setup()
				tui.fieldString(controller.getField) shouldEqual "                      1 1 1 1 1 1\n    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5\n    -----------------------------\n1  |# # # # # # # # # # # # # # #"
			}
		}
		"when it has some matrix" should {
			val controller = SpieController(1, TestFieldFactory(Vector.tabulate(10, 10)((y, x) => Cell(false, x == 5))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"should lose" in {
				println(controller.getField.toString)
				tui.processLine("1 1")
				tui.processLine("6 1")
				observer.l shouldBe(LostEvent())
			}
		}
	}
}