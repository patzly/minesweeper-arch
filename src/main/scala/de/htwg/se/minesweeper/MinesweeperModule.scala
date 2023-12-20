package de.htwg.se.minesweeper

import com.google.inject.AbstractModule
import com.google.inject.name.Names

import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.baseController._
import de.htwg.se.minesweeper.model.fieldComponent._
import de.htwg.se.minesweeper.model.fieldComponent.field._
import scala.util.Random

class MinesweeperModule extends AbstractModule {
    override def configure(): Unit = {
        bind(classOf[FieldFactory]).toInstance(RandomFieldFactory(Random()))
        bind(classOf[ControllerInterface]).to(classOf[BaseController]).asEagerSingleton()
    }
}
