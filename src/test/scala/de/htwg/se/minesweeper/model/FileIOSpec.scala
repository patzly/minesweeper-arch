package de.htwg.se.minesweeper.model

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model.fieldComponent.field._
import de.htwg.se.minesweeper.model.FileIOComponent.XML
import de.htwg.se.minesweeper.model.FileIOComponent.JSON
import de.htwg.se.minesweeper.model.FileIOComponent.Flexible

class FileIOSpec extends AnyWordSpec {
    private val testGameState = GameState(0, 3, Field(Vector(Vector(Cell(true, true)))), 1.0, 1, 1, false, List(Field(Vector(Vector(Cell(false, true))))), List(Field(Vector(Vector(Cell(true, true))))))
    private val emptyGameState = GameState(0, 0, Field(Vector.empty), 0, 0, 0)
    private val emptyGameState2 = GameState(0, 0, Field(Vector(Vector.empty)), 0, 0, 0)

    def fullEqual(gs: GameState, gs2: GameState): Unit = {
        gs.field should equal(gs2.field)
        gs.maxUndos should equal(gs2.maxUndos)
        gs.undos should equal(gs2.undos)
        gs.width should equal(gs2.width)
        gs.height should equal(gs2.height)
        gs.redoFields should equal(gs2.redoFields)
        gs.undoFields should equal(gs2.undoFields)
        gs.firstMove should equal(gs2.firstMove)
        gs.bombChance should equal(gs2.bombChance)
        gs should equal(gs2)
    }

    "a XML FileIO" when {
        val fileIO = XML.FileIO()
        "saving and loading a gameState with an empty field" should {
            fileIO.save(emptyGameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load an empty gameState" in {
                fullEqual(loadedGameState.get, emptyGameState)
            }
            fileIO.save(emptyGameState2, "test.xml")
            val loadedGameState2 = fileIO.load("test.xml")
            "load a second empty gameState" in {
                fullEqual(loadedGameState2.get, emptyGameState2)
            }
        }
        "saving and loading a gameState" should {
            fileIO.save(testGameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load the correct gameState" in {
                fullEqual(loadedGameState.get, testGameState)
            } 
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(testGameState, "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        }
    }
    "a JSON FileIO" when {
        val fileIO = JSON.FileIO()
        "saving and loading a gameState with an empty field" should {
            fileIO.save(emptyGameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load an empty gameState" in {
                fullEqual(loadedGameState.get, emptyGameState)
            }
            fileIO.save(emptyGameState2, "test.json")
            val loadedGameState2 = fileIO.load("test.json")
            "load a second empty gameState" in {
                fullEqual(loadedGameState2.get, emptyGameState2)
            }
        }
        "saving and loading a gameState" should {
            fileIO.save(testGameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                fullEqual(loadedGameState.get, testGameState)
            } 
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(testGameState, "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        }
    }
    "a Flexible FileIO" when {
        val fileIO = Flexible.FileIO()
        "saving and loading a gameState as xml" should {
            fileIO.save(testGameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load the correct gameState" in {
                fullEqual(loadedGameState.get, testGameState)
            } 
        }
        "saving and loading a gameState as json" should {
            fileIO.save(testGameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                fullEqual(loadedGameState.get, testGameState)
            }
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(testGameState, "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        }
    }
}