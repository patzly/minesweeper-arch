package minesweeper

import scala.util.Random
import model.*
import view.Tui
import controller.FieldController
import view.Gui
import javafx.application.Application.launch
import scalafx.application.JFXApp3
import scala.concurrent.Await
import scala.concurrent.Future
import scalafx.application.AppHelper3

def main(args: Array[String]): Unit = {
	val width = 16
	val height = 16
	val bomb_chance = 0.15f
	val rand = Random()

	val controller = FieldController(RandomFieldFactory(height, width, rand, bomb_chance))
	val tui = Tui(controller)
	val gui = Gui(controller)

	implicit val context = scala.concurrent.ExecutionContext.global
	val f = Future {
		gui.main(Array[String]())
	}
	
	// wait for gui to be ready
	// TODO: find a better way to do this
	while (!gui.ready()) {Thread.sleep(10)}

	controller.setup()
	tui.play()
	Await.ready(f, scala.concurrent.duration.Duration.Inf)
}