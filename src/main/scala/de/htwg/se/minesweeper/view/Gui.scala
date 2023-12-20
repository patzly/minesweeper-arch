package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.observer.Observer
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene

class Gui(controller: ControllerInterface) extends JFXApp3 with Observer[Event] with EventVisitor {
	controller.addObserver(this)

	private var gui_thread_ready = false
	private var gameScene: Option[GameScene] = None

	override def start(): Unit = {
		stage = new JFXApp3.PrimaryStage {
			minWidth = 700
			minHeight = 600
			title = "Minesweeper"
			scene = MainScene(controller)
			onCloseRequest = e => controller.exit()
		}

		gui_thread_ready = true
	}

	override def update(e: Event): Unit = {
		e match {
			case SetupEvent() => if gui_thread_ready then Platform.runLater(() => e.accept(this))
			case _ => Platform.runLater(() => e.accept(this))
		}
	}

	override def visitExit(event: ExitEvent): Unit = {
		// close the gui
		stage.close()
		System.exit(0)
	}

	override def visitLost(event: LostEvent): Unit = {
		gameScene.get.showLossScreen()
	}

	override def visitWon(event: WonEvent): Unit = {
		gameScene.get.showWinScreen()
	}

	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
		// update the gui
		gameScene.get.update(event)
	}

	override def visitSetup(event: SetupEvent): Unit = {
		gameScene = None
		stage.setScene(MainScene(controller))
	}

	override def visitStartGame(event: StartGameEvent): Unit = {
		gameScene = Some(GameScene(controller))
		stage.setScene(gameScene.get)
	}
}