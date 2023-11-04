package minesweeper.controller

import minesweeper.controller._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException
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

class FieldControllerSpec extends AnyWordSpec {
    "A FieldController" when {
        "it has a single cell field" should {
            val controller = FieldController(1, 1, (x, y) => Cell(false, false))
            var observer = TestObserver()
            controller.addObserver(observer)

            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("#")
            }
            "flag the cell" in { // has to be tested before reveal() is called!
                controller.flag(0, 0)
                observer.f.toString shouldBe ("⚑")
            }
            "reveal the cell" in {
                controller.reveal(0, 0)
                observer.f.toString shouldBe("☐")
                observer.w shouldBe(Event.Won)
            }
            "throw an exception" in {
                val thrown = intercept[IndexOutOfBoundsException] {
                    controller.reveal(1, 1)
                }
                thrown.getMessage shouldBe("Indices (1, 1) out of bounds for field of dimension (1, 1)")
            }
            "send an exit Event" in {
                controller.exit()
                observer.e shouldBe(Event.Exit)
            }
        }
        "it has a multi cell field" should {
            val controller = FieldController(3, 3, (x, y) => Cell(false, x == 0))
            var observer = TestObserver()
            controller.addObserver(observer)
            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("# # #\n# # #\n# # #")
            }
            "reveal the cell recursively" in {
                controller.reveal(2, 0)
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.w shouldBe(Event.Won)
                controller.reveal(0, 0)
                observer.f.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.l shouldBe(Event.Lost)
            }
        }
        "it has another multi cell field" should {
            var i = 0
            val controller = FieldController(3, 3, (x, y) => Cell(false, {
                if ((x, y)) == (2, 0) && i < 3 then
                    i += 1
                    true
                else x == 0
            }))
            var observer = TestObserver()
            controller.addObserver(observer)

            "without revealing the cell" in {
                controller.setup()
                observer.f.toString shouldBe("# # #\n# # #\n# # #")
            }
            "make sure the cell revealed first is not a bomb and then reveal recursively" in {
                controller.reveal(2, 0)
                observer.f.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                observer.w shouldBe(Event.Won)
            }
        }
    }
}