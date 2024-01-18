package de.htwg.se.minesweeper.model.FileIOComponent

import de.htwg.se.minesweeper.model.GameState
import scala.util.Try
import java.nio.file.Paths

trait FileIOInterface {
    def load(path: String): Try[GameState]
    def save(state: GameState, path: String): Try[Unit]
}

object FileExtension {
    // returns the file extension of a path
    def get(path: String): String = {
        val fileName = Paths.get(path).getFileName
        fileName.toString.split("\\.").last
    }
}

