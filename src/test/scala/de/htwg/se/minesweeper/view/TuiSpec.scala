package de.htwg.se.minesweeper.view

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.spyController.SpyController
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.model.fieldComponent.field._
import scala.concurrent._
import scala.concurrent.duration._

class TestObserver extends Observer[Event] with EventVisitor {
	var w: WonEvent = null
	var l: LostEvent = null
	override def update(ev: Event): Unit = {
		ev.accept(this)
	}

	override def visitStartGame(event: StartGameEvent): Unit = {}
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
		"play is called on input q" should {
			"quit" in {
				val controller = SpyController(TestFieldFactory(Vector(Vector(Cell(false, false)))))
				val tui = Tui(controller)
				val observer = TestObserver()
				controller.addObserver(observer)

				val out = new java.io.ByteArrayOutputStream()
				implicit val ec = ExecutionContext.global
				Await.result(Future {
					Console.withOut(out) {
						Console.withIn(new java.io.StringReader("q"))(tui.play())
					}
				}, 2.minutes)

				out.toString should include("Goodbye!")
			}
		}
		"it has an empty field" should {
			val controller = SpyController(TestFieldFactory(Vector.empty))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"have an empty fieldSting" in {
				tui.fieldString(controller.getGameState.field) shouldBe ""
			}
		}
		"it has a single cell field" should {
			val controller = SpyController(TestFieldFactory(Vector(Vector(Cell(false, false)))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"start correctly" should {
				controller.setup()
				tui.processLine("Invalid!")
				tui.processLine("fail: not a number")
				tui.processLine("0 0 0 0") // should fail
				tui.processLine("1 1 0 1")
			}
			"without revealing the cell" in {
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1\n   -\n1 |#"
				controller.getGameState.field.toString shouldEqual("#")
				tui.processLine("abc 2")
				controller.getGameState.field.toString shouldEqual("#")
				tui.processLine("2")
				controller.getGameState.field.toString shouldEqual("#")
			}
			"after redoing an empty stack" in {
				tui.processLine("r")
				controller.getGameState.field.toString shouldEqual("#")
			}
			"after flagging the cell" in {
				tui.processLine("1 1 flag")
				controller.getGameState.field.toString shouldEqual("⚑")
				tui.processLine("2 2 flag")
				controller.getGameState.field.toString shouldEqual("⚑")
			}
			"after undoing the flag" in {
				tui.processLine("u")
				controller.getGameState.field.toString shouldEqual("#")
				tui.processLine("u")
				controller.getGameState.field.toString shouldEqual("#")
			}
			"after undoing an empty stack" in {
				tui.processLine("u")
				controller.getGameState.field.toString shouldEqual("#")
			}
			"after redoing the flag" in {
				tui.processLine("r")
				controller.getGameState.field.toString shouldEqual("⚑")
			}
			"after redoing again" in {
				tui.processLine("r")
				controller.getGameState.field.toString shouldEqual("⚑")
			}
			"after flagging the cell again" in {
				tui.processLine("1 1 flag")
				controller.getGameState.field.toString shouldEqual("#")
			}
			"after revealing out of bounds" in {
				tui.processLine("0 0")
				controller.getGameState.field.toString shouldEqual "#"
			}
			"after revealing the cell" in {
				tui.processLine("1 1")
				controller.getGameState.field.toString shouldEqual("☐")
				observer.w shouldBe(WonEvent())
				tui.processLine("2 2")
				controller.getGameState.field.toString shouldEqual("☐")
			}
			"after going to menu" in {
				tui.processLine("menu")
			}
			"after quitting" in {
				tui.processLine("q")
				controller.didExit shouldBe(true)
			}
		}
		"it has a multi cell field" should {
			val controller = SpyController(TestFieldFactory(Vector.tabulate(3, 3)((y, x) => Cell(false, x == 0))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"without revealing the cell" in {
				controller.setup()
				controller.startGame(3, 3, 0, 1)
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc 2")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.processLine("abc")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
			}

			"after revealing some cells recursively" in {
				tui.processLine("3 3")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				tui.processLine("4 4")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")

				tui.processLine("2 1")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"

				observer.w shouldBe(WonEvent())
			}
			"after winning and retrying" in {
				tui.processLine("y")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"when winning" in {
				tui.processLine("3 3")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
			}
			"after retrying" in {
				tui.processLine("y")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"when retrying without having lost or won" in {
				tui.processLine("y")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
				tui.processLine("n")
				controller.getGameState.field.toString shouldEqual("# # #\n# # #\n# # #")
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# # #\n2 |# # #\n3 |# # #"
			}
			"after winning and not retrying" in {
				tui.processLine("3 3")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
				tui.processLine("n")
				controller.didExit shouldBe(true)
			}
			"after winning and quitting" in {
				controller.setup()
				controller.startGame(3, 3, 0, 1)
				tui.processLine("3 3")
				controller.getGameState.field.toString shouldEqual("# 2 ☐\n# 3 ☐\n# 2 ☐")
				observer.w shouldBe(WonEvent())
				tui.fieldString(controller.getGameState.field) shouldEqual "                     \n   1 2 3\n   -----\n1 |# 2 ☐\n2 |# 3 ☐\n3 |# 2 ☐"
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
			val controller = SpyController(TestFieldFactory(Vector.tabulate(1, 15)((y, x) => Cell(false, x == 2))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)

			"print correctly" in {
				controller.setup()
				controller.startGame(15, 1, 0, 1)
				tui.fieldString(controller.getGameState.field) shouldEqual "                      1 1 1 1 1 1\n    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5\n    -----------------------------\n1  |# # # # # # # # # # # # # # #"
			}
		}
		"when it has some matrix" should {
			val controller = SpyController(TestFieldFactory(Vector.tabulate(10, 10)((y, x) => Cell(false, x == 5))))
			val tui = Tui(controller)
			val observer = TestObserver()
			controller.addObserver(observer)
			controller.startGame(10, 10, 0, 1)

			"should lose" in {
				println(controller.getGameState.field.toString)
				tui.processLine("1 1")
				tui.processLine("6 1")
				observer.l shouldBe(LostEvent())
			}
			"retry and go to menu" in {
				tui.processLine("y")
				tui.processLine("menu")
			}
		}
	}
}