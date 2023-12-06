package minesweeper

import model.RandomFieldFactory
import controller.FieldController
import view.Tui
import view.Gui

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