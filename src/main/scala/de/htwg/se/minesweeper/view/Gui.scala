package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.observer.Observer
import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{IntegerProperty, BooleanProperty, StringProperty}
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.scene.{Node, Scene}
import scalafx.scene.text.Text

import java.util.{Timer, TimerTask}
import javafx.scene.layout.GridPane as JGridPane
import javafx.scene.input.MouseButton

class Gui(controller: ControllerInterface) extends JFXApp3 with Observer[Event] with EventVisitor {
	controller.addObserver(this)

	private val time_prop = IntegerProperty(0)
	private val undo_prop = IntegerProperty(controller.getUndos)
	private val cant_undo_prop = BooleanProperty(controller.cantUndo)
	private val redo_prop = BooleanProperty(controller.cantRedo)

	private val end_screen_visible = BooleanProperty(false)
	private val end_screen_text = StringProperty("")

	private final val stylesheet = getClass.getResource("/style.css").toExternalForm

	private var gui_thread_ready = false

	override def start(): Unit = {
		val t = new java.util.Timer()
		val task = new java.util.TimerTask {
			def run(): Unit = time_prop.value = time_prop.value + 1
		}
		t.schedule(task, 1000L, 1000L)

		stage = new JFXApp3.PrimaryStage {
			minWidth = 700
			minHeight = 600
			title = "Minesweeper"
			scene = makeMainScene()
			onCloseRequest = e => controller.exit()
		}

		gui_thread_ready = true
	}

	private def makeMainScene(): Scene = {
		val controls = new GridPane() { id = "main-controls-pane" }
		val width_spinner = new Spinner[Int](1, 32, 8)
		val height_spinner = new Spinner[Int](1, 32, 8)
		val bomb_spinner = new Spinner[Double](0.0, 1.0, 0.15, 0.01)
		val undo_spinner = new Spinner[Int](0, 5, 3)

		controls.addColumn(0, new Label("Breite"), new Label("Höhe"), new Label("Bomben Verteilung"), new Label("Anzahl Undos"))
		controls.addColumn(1, width_spinner, height_spinner, bomb_spinner, undo_spinner)

		new Scene {
			stylesheets = List(stylesheet)
			root = new BorderPane {
				id = "main"
				top = new HBox(new Text("Minesweeper") {
					styleClass = Seq("h1", "text-center", "bold", "white")
				}) {
					id = "main-top"
					maxWidth = Double.MaxValue
				}
				center = new FlowPane {
					id = "main-center"
					children = Seq(
						controls,
						new Button("Spielen") {
							onMouseClicked = e => controller.startGame(width_spinner.getValue, height_spinner.getValue, bomb_spinner.getValue.toFloat, undo_spinner.getValue)
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
	}

	private def makeGameScene(field: FieldInterface): Scene = {
		// create grid
		val grid = new GridPane {
			id = "game-cell-grid"
		}

		val (gridWidth, gridHeight) = field.dimension
		for (ix <- 0 until gridWidth) {
			for (iy <- 0 until gridHeight) {
				grid.add(new Button {
					styleClass = Seq("cell")
					onMouseClicked = e => e.getButton match {
						case MouseButton.PRIMARY => controller.reveal(ix, iy)
						case MouseButton.SECONDARY => controller.flag(ix, iy)
						case _ => ()
					}
					prefWidth <== Bindings.min(grid.widthProperty().divide(gridWidth.doubleValue), grid.heightProperty().divide(gridHeight.doubleValue))
					prefHeight <== Bindings.min(grid.widthProperty().divide(gridWidth.doubleValue), grid.heightProperty().divide(gridHeight.doubleValue))
				}, ix, iy)
			}
		}

		new Scene {
			stylesheets = List(stylesheet)
			root = new BorderPane {
				id = "game"
				top = new FlowPane {
					id = "game-top"
					children = Seq(
						new Text {
							styleClass = Seq("h2", "text-center", "bold", "white")
							text <== undo_prop.asString("Undos: %d")
						},
						new Text {
							styleClass = Seq("h2", "text-center", "bold", "white")
							text <== time_prop.asString("Zeit: %ds")
						}
					)
				}
				center = new StackPane {
					id = "game-center"
					children = Seq(
						grid,
						new FlowPane {
							id = "game-end-screen"
							visible <== end_screen_visible
							children = Seq(
								new Text {
									text <== end_screen_text
									styleClass = Seq("h1", "text-center", "bold", "white")
								},
								new Button("Retry") {
									id = "game-retry-btn"
									onMouseClicked = e => {
										end_screen_visible.setValue(false)
										val (width, height) = controller.getField.dimension
										controller.startGame(width, height, controller.getBombChance, controller.getMaxUndos)
									}
								}
							)
						}
					)
				}
				bottom = new FlowPane {
					id = "game-bottom"
					children = Seq(
						new Button("Zum Menü") {
							onMouseClicked = e => controller.setup()
						},
						new Button("Undo") {
							disable <== end_screen_visible.or(cant_undo_prop)
							onMouseClicked = e => controller.undo()
						},
						new Button("Redo") {
							disable <== end_screen_visible.or(redo_prop)
							onMouseClicked = e => controller.redo()
						}
					)
				}
			}
		}
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
		// show the lost screen
		// and a retry button
		end_screen_visible.setValue(true)
		end_screen_text.setValue("You lost!")
	}

	override def visitWon(event: WonEvent): Unit = {
		// show the won screen
		// and a retry button
		end_screen_visible.setValue(true)
		end_screen_text.setValue("You won!")
	}

	override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
		// update the gui
		Platform.runLater(undo_prop.setValue(controller.getUndos))
		Platform.runLater(cant_undo_prop.setValue(controller.cantUndo))
		Platform.runLater(redo_prop.setValue(controller.cantRedo))

		// update the grid
		val grid = stage.getScene.lookup("#game-cell-grid").asInstanceOf[JGridPane] // get grid by ID
		grid.getChildren.forEach(node => {
			val button = node.asInstanceOf[javafx.scene.control.Button]
			val cell = event.field.getCell(JGridPane.getColumnIndex(button), JGridPane.getRowIndex(button)).get
			button.getStyleClass.retainAll("cell")
			button.setViewOrder(0)

			if cell.isFlagged then button.getStyleClass.add("flagged")
			else if cell.isRevealed then
				button.setViewOrder(1)
				if cell.isBomb then button.getStyleClass.add("bomb")
				else if cell.nearbyBombs != 0 then button.getStyleClass.add("n" + cell.nearbyBombs.toString)
				else button.getStyleClass.add("revealed")
			else
				button.getStyleClass.add("unrevealed")
		})
	}

	override def visitSetup(event: SetupEvent): Unit = {
		end_screen_visible.setValue(false)
		stage.setScene(makeMainScene())
	}

	override def visitStartGame(event: StartGameEvent): Unit = {
		Platform.runLater({
			time_prop.value = 0
			end_screen_visible.setValue(false)
			undo_prop.setValue(controller.getUndos)
			cant_undo_prop.setValue(controller.cantUndo)
			redo_prop.setValue(controller.cantRedo)
			stage.setScene(makeGameScene(controller.getField))
		})
	}
}