package de.htwg.se.minesweeper

import de.htwg.se.minesweeper.model.fieldComponent.field.RandomFieldFactory
import de.htwg.se.minesweeper.controller.baseController.BaseController
import de.htwg.se.minesweeper.view.Tui
import de.htwg.se.minesweeper.view.Gui

import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.Future

@main
def main(): Unit = {
	val width = 16
	val height = 16
	val bomb_chance = 0.15f
	val undos = 3
	val rand = Random()

	val controller = BaseController(undos, RandomFieldFactory(height, width, rand, bomb_chance))
	val tui = Tui(controller)
	val gui = Gui(controller)
	controller.setup()

	implicit val context = scala.concurrent.ExecutionContext.global
	val f = Future {
		gui.main(Array[String]())
	}

	tui.play()
	Await.ready(f, scala.concurrent.duration.Duration.Inf)
}