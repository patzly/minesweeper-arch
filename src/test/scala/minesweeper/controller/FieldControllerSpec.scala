package minesweeper.controller

import minesweeper.controller._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException
import minesweeper.observer.Observer
import scala.util.Success
import scala.util.Failure

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

class FieldControllerSpec extends AnyWordSpec {
    "A FieldController" when {
        "it has a single cell field" should {
            val controller = FieldController(TestFieldFactory(Vector(Vector(Cell(false, false)))))
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
            }
            "after redoing the flag" in {
                controller.redo() shouldBe(Success(()))
                observer.f.toString shouldBe("⚑")
            }
            "return Failure (firstMove)" in {
                controller.reveal(1, 1) shouldBe a[Failure[IndexOutOfBoundsException]]
            }
            "reveal the cell" in {
                controller.reveal(0, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("☐")
                observer.w shouldBe(Event.Won)
            }
            "after undoing the reveal" in {
                controller.undo() shouldBe(Success(()))
                observer.f.toString shouldBe("⚑")
            }
            "after redoing the reveal" in {
                controller.redo() shouldBe(Success(()))
                observer.f.toString shouldBe("☐")
            }
            "after redoing an empty stack" in {
                controller.redo() shouldBe a [Failure[NoSuchElementException]]
                observer.f.toString shouldBe("☐")
            }
            "return Failure" in {
                controller.reveal(1, 1) shouldBe a [Failure[IndexOutOfBoundsException]]
                observer.f.toString shouldBe("☐")
            }
            "send an exit Event" in {
                controller.exit()
                observer.e shouldBe(Event.Exit)
            }
        }
        "it has a multi cell field" should {
            val controller = FieldController(TestFieldFactory(Vector.tabulate(3, 3)((y, x) => Cell(false, x == 0))))
            val observer = TestObserver()
            controller.addObserver(observer)
            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("# # #\n# # #\n# # #")
            }
            "reveal the cell recursively" in {
                controller.reveal(2, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.w shouldBe(Event.Won)
                controller.reveal(0, 0) shouldBe(Success(()))
                observer.f.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.l shouldBe(Event.Lost)
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
            val controller = FieldController(GeneratorTestFieldFactory(3, 3, (y, x) => Cell(false, {
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
                observer.w shouldBe(Event.Won)
            }
        }
    }
}