package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.observer.Observer
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

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
			icons.add(new Image("file:icon.png"))
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

	override def visitLost(event: LostEvent): Unit = gameScene.get.showLossScreen()
	override def visitWon(event: WonEvent): Unit = gameScene.get.showWinScreen()
	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = gameScene.get.update(event.field)

	override def visitSetup(event: SetupEvent): Unit = {
		gameScene = None
		stage.setScene(MainScene(controller))
	}

	override def visitStartGame(event: StartGameEvent): Unit = {
		gameScene = Some(GameScene(controller))
		gameScene.get.update(event.field)
		stage.setScene(gameScene.get)
	}
}

object Gui {
	private val fc = new FileChooser {
		title = "Spielstandsdatei auswählen"
		extensionFilters.add(new ExtensionFilter("Spielstand", Seq("*.xml", "*.json")))
		initialFileName = "Spielstand.json"
	}

	def openFileDialog(window: javafx.stage.Window): java.io.File = {
		fc.showOpenDialog(window)
	}

	def saveFileDialog(window: javafx.stage.Window): java.io.File = {
		fc.showSaveDialog(window)
	}
}