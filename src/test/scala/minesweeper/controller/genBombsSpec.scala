package minesweeper.controller

import minesweeper.model._
import minesweeper.controller._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class NRand(val result: Int) extends scala.util.Random {
    override def nextInt(n: Int): Int = result
}

class GenBombSpec extends AnyWordSpec {
    "genBombsRand" when {
        "used with a generator that always returns 0" should {
            val rand = new NRand(0)
            val genbomb = genbombRand(rand, 0.25f)

            "always return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(true, true))
                genbomb(1, 1) shouldBe(Cell(true, true))
                genbomb(0, 1) shouldBe(Cell(true, true))
            }
        }
        "used with a generator that never returns 0" should {
            val rand = new NRand(1)
            val genbomb = genbombRand(rand, 0.25f)

            "never return a bomb" in {
                genbomb(0, 0) shouldBe(Cell(true, false))
                genbomb(1, 1) shouldBe(Cell(true, false))
                genbomb(0, 1) shouldBe(Cell(true, false))
            }
        }
    }
}