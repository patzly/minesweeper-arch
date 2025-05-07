package de.htwg.se.minesweeper

import com.google.inject.AbstractModule
import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.controller.baseController.*
import de.htwg.se.minesweeper.model.fieldComponent.*
import de.htwg.se.minesweeper.model.fieldComponent.field.*

import scala.util.Random
import de.htwg.se.minesweeper.model.FileIOComponent.*
import de.htwg.se.minesweeper.database.{DatabaseModule, FileIOGameStateDao, GameStateDao}

import scala.concurrent.ExecutionContext

class MinesweeperModule(implicit executionContext: ExecutionContext) extends AbstractModule {

  override def configure(): Unit = {
    val db = DatabaseModule.init("gameState.db")

    bind(classOf[FileIOInterface]).toInstance(FileIOGameStateDao(db))
    bind(classOf[FieldFactory]).toInstance(RandomFieldFactory(Random()))
    bind(classOf[ControllerInterface])
      .to(classOf[BaseController])
      .asEagerSingleton()
  }
}
