package minesweeper.controller

import minesweeper.controller._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException

class FieldControllerSpec extends AnyWordSpec {
    "A FieldController" when {
        "it has a single cell field" should {
            val controller = FieldController(1, 1, (x, y) => Cell(false, false))

            "without revealing the cell" in {
                controller.field.toString shouldEqual("#")
            }
            "flag the cell" in { // has to be tested before reveal() is called!
                controller.flag(0, 0)
                controller.field.toString shouldBe ("⚑")
            }
            "reveal the cell" in {
                controller.reveal(0, 0)
                controller.field.toString shouldBe("☐")
            }
            "throw an Exception" in {
                val thrown = intercept[IndexOutOfBoundsException] {
                    controller.reveal(1, 1)
                }
                thrown.getMessage shouldEqual("Indices (1, 1) out of bounds for field of dimension (1, 1)")
            }
        }
        "it has a multi cell field" should {
            val controller = FieldController(3, 3, (x, y) => Cell(false, x == 0))
            "without revealing the cell" in {
                controller.field.toString shouldBe("# # #\n# # #\n# # #")
            }
            "reveal the cell recursively" in {
                controller.reveal(2, 0)
                controller.field.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
                controller.reveal(0, 0)
                controller.field.toString shouldBe("☒ 2 ☐\n# 3 ☐\n# 2 ☐")
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
            "without revealing the cell" in {
                controller.field.toString shouldBe("# # #\n# # #\n# # #")
            }
            "make sure the cell revealed first is not a bomb and then reveal recursively" in {
                controller.reveal(2, 0)
                controller.field.toString shouldBe("# 2 ☐\n# 3 ☐\n# 2 ☐")
            }
        }
    }
}