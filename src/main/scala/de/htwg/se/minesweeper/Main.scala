package de.htwg.se.minesweeper

import de.htwg.se.minesweeper.model.RandomFieldFactory
import de.htwg.se.minesweeper.controller.FieldController
import de.htwg.se.minesweeper.view.Tui
import de.htwg.se.minesweeper.view.Gui

import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.Future

def main(args: Array[String]): Unit = {
	val width = 16
	val height = 16
	val bomb_chance = 0.15f
	val rand = Random()

	val controller = FieldController(RandomFieldFactory(height, width, rand, bomb_chance))
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