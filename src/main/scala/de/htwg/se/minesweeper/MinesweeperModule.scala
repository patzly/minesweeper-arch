package de.htwg.se.minesweeper

import com.google.inject.AbstractModule

import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.baseController._
import de.htwg.se.minesweeper.model.fieldComponent._
import de.htwg.se.minesweeper.model.fieldComponent.field._
import scala.util.Random
import de.htwg.se.minesweeper.model.FileIOComponent._

import de.htwg.se.minesweeper.model.FileIOComponent.Flexible.FileIO
import com.google.inject.name.Names

class MinesweeperModule extends AbstractModule {
    override def configure(): Unit = {
        // bind the named parameter "defaultSavePath" to the value "save.xml"
        bindConstant().annotatedWith(Names.named("defaultSavePath")).to("minesweeper.save.xml")

        bind(classOf[FileIOInterface]).to(classOf[FileIO])
        bind(classOf[FieldFactory]).toInstance(RandomFieldFactory(Random()))
        bind(classOf[ControllerInterface]).to(classOf[BaseController]).asEagerSingleton()
    }
}
