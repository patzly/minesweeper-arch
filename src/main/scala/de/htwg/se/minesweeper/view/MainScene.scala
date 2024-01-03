package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.scene.text.Text

case class MainScene(controller: ControllerInterface) extends Scene {
	private val controls = new GridPane() {
		id = "main-controls-pane"
	}
	private val width_spinner = new Spinner[Int](1, 32, 8)
	private val height_spinner = new Spinner[Int](1, 32, 8)
	private val bomb_spinner = new Spinner[Double](0.0, 1.0, 0.15, 0.01)
	private val undo_spinner = new Spinner[Int](0, 10, 3)
	private val load_path_input = new TextField()

	controls.addColumn(0, new Label("Breite"), new Label("Höhe"), new Label("Bomben Verteilung"), new Label("Anzahl Undos"))
	controls.addColumn(1, width_spinner, height_spinner, bomb_spinner, undo_spinner)

	stylesheets = List(getClass.getResource("/style.css").toExternalForm)
	root = new BorderPane {
		id = "main"
		top = new HBox(new Text("Minesweeper") {
			styleClass = Seq("h1", "text-center", "bold", "white")
		}) {
			id = "main-top"
		}
		center = new FlowPane {
			id = "main-center"
			children = Seq(
				controls,
				new Button("Spielen") {
					onMouseClicked = e => controller.startGame(width_spinner.getValue, height_spinner.getValue, bomb_spinner.getValue.toFloat, undo_spinner.getValue)
				},
				load_path_input,
				new Button("Speicherstand Laden") {
					onMouseClicked = e => controller.loadGame(load_path_input.getText)
				}
			)
		}
		bottom = new FlowPane {
			id = "main-bottom"
			children = Seq(
				new Text("Software Engineering Projekt WS23/24") {
					styleClass = Seq("h2", "text-center", "bold", "white")
				}, new Text("Leon Gies und Hendrik Ziegler") {
					styleClass = Seq("h3", "text-center", "bold", "white")
				}
			)
		}
	}
}
