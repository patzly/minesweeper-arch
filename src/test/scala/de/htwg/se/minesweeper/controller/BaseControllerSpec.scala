package de.htwg.se.minesweeper.controller

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.observer.Observer
import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.baseController._
import de.htwg.se.minesweeper.model.fieldComponent.field._
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.observer._
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.model.fieldComponent.field.TestFieldFactory

class TestObserver extends Observer[Event] with EventVisitor {
    var f: FieldInterface = null
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

class BaseControllerSpec extends AnyWordSpec {
    "A BaseController" when {
        "it has a single cell field" should {
            val controller = BaseController(2, TestFieldFactory(Vector(Vector(Cell(false, false)))))
            val observer = TestObserver()
            controller.addObserver(observer)

            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("#")
            }
            "after undoing an empty stack" in {
                val fail = controller.undo() shouldBe a [Failure[NoSuchElementException]]
                observer.f.toString shouldBe("#")
            }
            "flag the cell" in { // has to be tested before reveal() is called!
                controller.flag(0, 0) shouldBe(Success(()))
                observer.f.toString shouldBe ("⚑")
            }
            "after undoing the flag" in {
                controller.undo() shouldBe(Success(()))
                observer.f.toString shouldBe("#")
                controller.getUndos shouldBe 1
                controller.cantRedo shouldBe false
            }
            "after redoing the flag" in {
                controller.redo() shouldBe(Success(()))
                observer.f.toString shouldBe("⚑")
                controller.cantRedo shouldBe true
            }
            "return Failure (firstMove)" in {
                controller.reveal(1, 1) shouldBe a[Failure[IndexOutOfBoundsException]]
            }
            "reveal the cell" in {
                controller.reveal(0, 0) shouldBe(Success(()))

                observer.f.toString shouldBe("☐")
                observer.w shouldBe(WonEvent())
            }
            "after undoing the reveal" in {
                controller.undo() shouldBe(Success(()))
                observer.f.toString shouldBe("⚑")
                controller.getUndos shouldBe 0
            }
            "after redoing the reveal" in {
                controller.redo() shouldBe(Success(()))
                observer.f.toString shouldBe("☐")
                controller.cantRedo shouldBe true
            }
            "throw after redoing an empty stack" in {
                controller.redo() shouldBe a [Failure[NoSuchElementException]]
                observer.f.toString shouldBe("☐")
            }
            "throw after undoing without any undos left" in {
                controller.undo() shouldBe a [Failure[RuntimeException]]
            }
            "return Failure" in {
                controller.reveal(1, 1) shouldBe a [Failure[IndexOutOfBoundsException]]
                observer.f.toString shouldBe("☐")
            }
            "send an exit Event" in {
                controller.exit()
                observer.e shouldBe(ExitEvent())
            }
        }
        "it has a multi cell field" should {
            val controller = BaseController(1, TestFieldFactory(Vector.tabulate(3, 3)((y, x) => Cell(false, x == 0))))
            val observer = TestObserver()
            controller.addObserver(observer)
            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("# # #\n# # #\n# # #")
            }
            "reveal the cell recursively" in {
                controller.reveal(2, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.w shouldBe(WonEvent())
                controller.reveal(0, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.l shouldBe(LostEvent())
            }
            "undo the last reveal" in {
                controller.undo() shouldBe(Success(()))
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
            }
            "redo the last reveal" in {
                controller.redo() shouldBe(Success(()))
                observer.f.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
            }
        }
        "it has another multi cell field" should {
            var i = 0
            val controller = BaseController(1, GeneratorTestFieldFactory(3, 3, (y, x) => Cell(false, {
                if ((x, y) == (2, 0) && i < 3) then
                    i += 1
                    true
                else x == 0
            })))
            val observer = TestObserver()
            controller.addObserver(observer)

            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("# # #\n# # #\n# # #")
            }
            "make sure the cell revealed first is not a bomb and then reveal recursively" in {
                controller.reveal(2, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.w shouldBe(WonEvent())
            }
        }
    }
}