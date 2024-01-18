package de.htwg.se.minesweeper.model.FileIOComponent.Flexible

import de.htwg.se.minesweeper.model.GameState
import de.htwg.se.minesweeper.model.FileIOComponent.FileIOInterface
import scala.util.{Try, Failure}
import de.htwg.se.minesweeper.model.FileIOComponent.XML
import de.htwg.se.minesweeper.model.FileIOComponent.JSON
import de.htwg.se.minesweeper.model.FileIOComponent.FileExtension

// this FileIO can load and save both XML and JSON
// and decides which one to use based on the file extension
class FileIO extends FileIOInterface {
    private val xmlIO = new XML.FileIO
    private val jsonIO = new JSON.FileIO
    
    def load(path: String): Try[GameState] = FileExtension.get(path) match {
        case "xml" => xmlIO.load(path)
        case "json" => jsonIO.load(path)
        case _ => Failure(new Exception("Unknown file extension, can only load .json or .xml"))
    }

    def save(state: GameState, path: String): Try[Unit] = FileExtension.get(path) match {
        case "xml" => xmlIO.save(state, path)
        case "json" => jsonIO.save(state, path)
        case _ => Failure(new Exception("Unknown file extension, can only save .json or .xml"))
    }
}
