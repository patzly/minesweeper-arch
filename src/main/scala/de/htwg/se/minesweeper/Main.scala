package de.htwg.se.minesweeper

import de.htwg.se.minesweeper.controller.ControllerInterface
import de.htwg.se.minesweeper.view.Tui
import de.htwg.se.minesweeper.view.Gui

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration

import com.google.inject.Guice

@main
def main(): Unit = {
	val injector = Guice.createInjector(new MinesweeperModule)
	val controller = injector.getInstance(classOf[ControllerInterface])

	val tui = Tui(controller)
	val gui = Gui(controller)
	controller.setup()

	implicit val context = ExecutionContext.global
	val f = Future {
		gui.main(Array[String]())
	}

	tui.play()
	Await.ready(f, Duration.Inf)
}