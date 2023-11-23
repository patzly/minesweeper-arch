package minesweeper.model

import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class NRand(val result: Int) extends scala.util.Random {
	override def nextInt(n: Int): Int = result
}

class RandomFieldFactorySpec  extends AnyWordSpec {
	"RandomFieldFactorySpec" when {
		"creates field with a random that always returns 0" should {
			val rand = new NRand(0)
			val field = RandomFieldFactory(2, 2, rand, 0.25f).createField()

			"always return a bomb" in {
				field.getCell(0, 0).get.isBomb shouldBe true
				field.getCell(1, 1).get.isBomb shouldBe true
				field.getCell(0, 1).get.isBomb shouldBe true
			}
		}
		"creates field with a random that never returns 0" should {
			val rand = new NRand(1)
			val field = RandomFieldFactory(2, 2, rand, 0.25f).createField()

			"never return a bomb" in {
				field.getCell(0, 0).get.isBomb shouldBe false
				field.getCell(1, 1).get.isBomb shouldBe false
				field.getCell(0, 1).get.isBomb shouldBe false
			}
		}
	}
}
