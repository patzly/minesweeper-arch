package de.htwg.se.minesweeper.model

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.model.Cell
import de.htwg.se.minesweeper.model.Cell
import scala.xml.Utility.trim
import scala.xml.XML

class CellSpec extends AnyWordSpec {
	"A Cell" when {
		"it's a bomb and revealed" should {
			val cell = Cell(true, true)

			"be printed as the bomb char" in {
				cell.toString() should be("☒")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>true</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>true</isRevealed><isBomb>true</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's a bomb and hidden" should {
			val cell = Cell(false, true)

			"be printed as the hidden char" in {
				cell.toString() should be("#")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>true</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>")
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>false</isRevealed><isBomb>true</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's a not bomb and revealed" should {
			val cell = Cell(true, false)

			"be printed as the revealed char" in {
				cell.toString() should be("☐")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's a not bomb and hidden" should {
			val cell = Cell(false, false)

			"be printed as the hidden char" in {
				cell.toString() should be("#")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's flagged and hidden" should {
			val cell = Cell(false, false, true)

			"be printed as the flag char" in {
				cell.toString should be("⚑")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's flagged and revealed" should {
			val cell = Cell(true, false, true)

			"be printed as the revealed char" in {
				cell.toString should be("☐")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's flagged, hidden and a bomb" should {
			val cell = Cell(false, true, true)

			"be printed as the flag char" in {
				cell.toString should be("⚑")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>false</isRevealed><isBomb>true</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>false</isRevealed><isBomb>true</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it's flagged, revealed and a bomb" should {
			val cell = Cell(true, true, true)

			"be printed as the bomb char" in {
				cell.toString should be("☒")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>true</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>true</isRevealed><isBomb>true</isBomb><isFlagged>true</isFlagged><nearbyBombs>0</nearbyBombs></cell>) should be(cell)
			}
		}

		"it has 4 bombs nearby and is hidden" should {
			val cell = Cell(false, false, false, 4)

			"be printed as the hidden char" in {
				cell.toString should be("#")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>4</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>false</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>4</nearbyBombs></cell>) should be(cell)
			}
		}

		"it has 4 bombs nearby and is revealed" should {
			val cell = Cell(true, false, false, 4)

			"be printed as the number 4" in {
				cell.toString should be("4")
			}

			"be converted to XML" in {
				val actualXML = XML.loadString(cell.toXML().toString)
				val expectedXML = XML.loadString("<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>4</nearbyBombs></cell>")

				actualXML should be(expectedXML)
			}

			"be created from XML" in {
				Cell.fromXML(<cell><isRevealed>true</isRevealed><isBomb>false</isBomb><isFlagged>false</isFlagged><nearbyBombs>4</nearbyBombs></cell>) should be(cell)
			}
		}
	}
}