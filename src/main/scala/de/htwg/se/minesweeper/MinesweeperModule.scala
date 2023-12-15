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
        val width = 16
        val height = 16
        val bomb_chance = 0.15f
        val undos = 3
        val rand = Random()

        bind(classOf[Int]).annotatedWith(Names.named("undos")).toInstance(undos)

        val field_factory = RandomFieldFactory(width, height, rand, bomb_chance)

        bind(classOf[FieldFactory]).toInstance(field_factory)
        bind(classOf[ControllerInterface]).to(classOf[BaseController]).asEagerSingleton()
    
    }
}
