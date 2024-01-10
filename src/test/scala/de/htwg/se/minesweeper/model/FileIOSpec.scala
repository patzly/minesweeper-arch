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
    "a XML FileIO" when {
        val fileIO = XML.FileIO()
        "saving and loading a gameState" should {
            val gameState = GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0)
            fileIO.save(gameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load the correct gameState" in {
                loadedGameState.get.field should equal(gameState.field)
                loadedGameState.get.maxUndos should equal(gameState.maxUndos)
                loadedGameState.get.undos should equal(gameState.undos)
                loadedGameState.get.width should equal(gameState.width)
                loadedGameState.get.height should equal(gameState.height)
                loadedGameState.get.redoFields should equal(gameState.redoFields)
                loadedGameState.get.undoFields should equal(gameState.undoFields)
                loadedGameState.get.firstMove should equal(gameState.firstMove)
                loadedGameState.get.bombChance should equal(gameState.bombChance)
                loadedGameState.get should equal(gameState)
            } 
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0), "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        
        }
    }
    "a JSON FileIO" when {
        val fileIO = JSON.FileIO()
        "saving and loading a gameState" should {
            val gameState = GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0)
            fileIO.save(gameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                loadedGameState shouldBe(Success(gameState))
            } 
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0), "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        
        }
    }
    "a Flexible FileIO" when {
        val fileIO = Flexible.FileIO()
        "saving and loading a gameState as xml" should {
            val gameState = GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0)
            fileIO.save(gameState, "test.xml")
            val loadedGameState = fileIO.load("test.xml")
            "load the correct gameState" in {
                loadedGameState.get shouldBe(gameState)
            } 
        }
        "saving and loading a gameState as json" should {
            val gameState = GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0)
            fileIO.save(gameState, "test.json")
            val loadedGameState = fileIO.load("test.json")
            "load the correct gameState" in {
                loadedGameState.get.field should equal(gameState.field)
                loadedGameState.get.maxUndos should equal(gameState.maxUndos)
                loadedGameState.get.undos should equal(gameState.undos)
                loadedGameState.get.width should equal(gameState.width)
                loadedGameState.get.height should equal(gameState.height)
                loadedGameState.get.redoFields should equal(gameState.redoFields)
                loadedGameState.get.undoFields should equal(gameState.undoFields)
                loadedGameState.get.firstMove should equal (gameState.firstMove)
                loadedGameState.get.bombChance should equal (gameState.bombChance)
                loadedGameState.get should equal(gameState)
            }
        }
        "saving or loading the wrong file extension" should {
            "return a Failure" in {
                fileIO.save(GameState(0, 0, Field(Vector(Vector(Cell(true, true)))), 0, 0, 0), "test.txt") shouldBe a[Failure[Exception]]
                fileIO.load("test.txt") shouldBe a[Failure[Exception]]
            }
        
        }
    }
}