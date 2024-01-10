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
    private val testGameState = GameState(0, 3, Field(Vector(Vector(Cell(true, true)))), 1.0, 1, 1, false, List(Field(Vector(Vector(Cell(false, true)))), Field(Vector(Vector(Cell(true, true))))))
    "a XML FileIO" when {
        val fileIO = XML.FileIO()
        "saving and loading a gameState" should {
            fileIO.save(testGameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load the correct gameState" in {
                loadedGameState.get.field should equal(testGameState.field)
                loadedGameState.get.maxUndos should equal(testGameState.maxUndos)
                loadedGameState.get.undos should equal(testGameState.undos)
                loadedGameState.get.width should equal(testGameState.width)
                loadedGameState.get.height should equal(testGameState.height)
                loadedGameState.get.redoFields should equal(testGameState.redoFields)
                loadedGameState.get.undoFields should equal(testGameState.undoFields)
                loadedGameState.get.firstMove should equal(testGameState.firstMove)
                loadedGameState.get.bombChance should equal(testGameState.bombChance)
                loadedGameState.get should equal(testGameState)
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
        "saving and loading a gameState" should {
            fileIO.save(testGameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                loadedGameState shouldBe Success(testGameState)
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
                loadedGameState.get shouldBe testGameState
            } 
        }
        "saving and loading a gameState as json" should {
            fileIO.save(testGameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                loadedGameState.get.field should equal(testGameState.field)
                loadedGameState.get.maxUndos should equal(testGameState.maxUndos)
                loadedGameState.get.undos should equal(testGameState.undos)
                loadedGameState.get.width should equal(testGameState.width)
                loadedGameState.get.height should equal(testGameState.height)
                loadedGameState.get.redoFields should equal(testGameState.redoFields)
                loadedGameState.get.undoFields should equal(testGameState.undoFields)
                loadedGameState.get.firstMove should equal (testGameState.firstMove)
                loadedGameState.get.bombChance should equal (testGameState.bombChance)
                loadedGameState.get should equal(testGameState)
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