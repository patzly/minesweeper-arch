package minesweeper.controller

import minesweeper.controller._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException

class FieldControllerSpec extends AnyWordSpec {
    "A FieldController" when {
        "it has a single cell field" should {
            val field = Field(1, 1, (x, y) => Cell(false, false))
            val controller = FieldController(field)

            "without revealing the cell" in {
                controller.field.toString shouldEqual("#")
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
            val field = Field(3, 3, (x, y) => Cell(false, false))
            val controller = FieldController(field)
            "without revealing the cell" in {
                controller.field.toString shouldBe("# # #\n# # #\n# # #")
            }
            "reveal the cell" in {
                controller.reveal(0, 0)
                controller.field.toString shouldBe("☐ # #\n# # #\n# # #")
                controller.reveal(2, 2)
                controller.field.toString shouldBe("☐ # #\n# # #\n# # ☐")
            }
        }
    }
}