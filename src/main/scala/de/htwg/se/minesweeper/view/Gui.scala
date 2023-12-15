package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface
import de.htwg.se.minesweeper.observer.Observer
import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.property.*
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.*
import scalafx.scene.text.Text
import java.util.{Timer, TimerTask}

import scala.util.{Failure, Success, Try}

class Gui(controller: ControllerInterface) extends JFXApp3 with Observer[Event] with EventVisitor {
	controller.addObserver(this)
	private var grid: Option[GridPane] = None

	private var images: Option[Map[String, Image]] = None

	private val time_prop = IntegerProperty(0)
	private val undo_prop = IntegerProperty(controller.getUndos)
	private val redo_prop = BooleanProperty(controller.cantRedo)

	private val end_screen_visible = BooleanProperty(false)
	private val end_screen_text = StringProperty("")

	private val stylesheet = getClass.getResource("/style.css").toExternalForm
	private val background_color = Color.rgb(38, 38, 38)
	
	private var gui_thread_ready = false
	override def start(): Unit = {
		val t = new java.util.Timer()
		val task = new java.util.TimerTask {
			def run(): Unit = time_prop.value = time_prop.value + 1
		}
		t.schedule(task, 1000L, 1000L)

		images = createImages() match {
			case Success(value) => Some(value)
			case Failure(e) => throw new Exception("Could not load images!")
		}

		stage = new JFXApp3.PrimaryStage {
			//    initStyle(StageStyle.Unified)
			title = "Minesweeper"
			scene = makeMainScene()
			onCloseRequest = e => {
				controller.exit()
			}
		}
		gui_thread_ready = true
	}

	private def createImages(): Try[Map[String, Image]] = {
		Try(Map(
			"unrevealed" -> new Image("file:img/unrevealed.png"),
			"revealed" -> new Image("file:img/revealed.png"),
			"flagged" -> new Image("file:img/flagged.png"),
			"bomb" -> new Image("file:img/bomb.png"),
			"1" -> new Image("file:img/1.png"),
			"2" -> new Image("file:img/2.png"),
			"3" -> new Image("file:img/3.png"),
			"4" -> new Image("file:img/4.png"),
			"5" -> new Image("file:img/5.png"),
			"6" -> new Image("file:img/6.png"),
			"7" -> new Image("file:img/7.png"),
			"8" -> new Image("file:img/8.png")
		))
	}

	private def makeMainScene(): Scene = {
		val controls = new GridPane() { id = "main-controls-pane" }
		controls.add(new Text("Breite") {styleClass = Seq("white")}, 0, 0)
		controls.add(new Text("Höhe") {styleClass = Seq("white")}, 0, 1)
		controls.add(new Text("Bomben Verteilung") {styleClass = Seq("white")}, 0, 2)
		val width_spinner = new Spinner[Int](1, 16, 8)
		val height_spinner = new Spinner[Int](1, 16, 8)
		val bomb_spinner = new Spinner[Double](0.0, 1.0, 0.15, 0.01)
		controls.add(width_spinner, 1, 0)
		controls.add(height_spinner, 1, 1)
		controls.add(bomb_spinner, 1, 2)

		new Scene {
			stylesheets = List(stylesheet)
			fill = background_color
			content = new BorderPane {
				hgrow = Priority.Always
				vgrow = Priority.Always
				id = "main"
				top = new HBox(new Text("Minesweeper") {
					styleClass = Seq("h1", "text-center", "bold", "white")
				}) {
					id = "main-top"
				}
				center = new FlowPane {
					children = Seq(
						controls,
						new Button("Spielen") {

							onMouseClicked = e => {
								val (width_val, height_val, bomb_chance) = (width_spinner.valueProperty().getValue, height_spinner.valueProperty().getValue, bomb_spinner.valueProperty().getValue)

								controller.startGame(width_val, height_val, bomb_chance.toFloat)
							}
						}
					)
					id = "main-center"
				}
				bottom = new FlowPane {
					children = Seq(
						new Text("Software Engineering Projekt WS23/24") {
							styleClass = Seq("h2", "text-center", "bold", "white")
						}, new Text("Leon Gies und Hendrik Ziegler") {
							styleClass = Seq("h3", "text-center", "bold", "white")
						}
					)
					id = "main-bottom"
				}
			}
		}
	}

	private def makeGameScene(gridPane: GridPane): Scene = {
		new Scene {
			stylesheets = List(stylesheet)
			fill = background_color
			content = new BorderPane() {
				padding = Insets(50)
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
					children = Seq(
						new FlowPane {
							alignment = Pos.Center
							columnHalignment = HPos.Center
							children = gridPane
						},
						new FlowPane {
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
										controller.startGame(width, height, controller.getBombChance)
									}
								}
							)
							id = "game-end-screen"
							visible <== end_screen_visible
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
							disable <== end_screen_visible.or(undo_prop.isEqualTo(0))
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
		Platform.runLater(redo_prop.setValue(controller.cantRedo))
		updateGrid(event.field)
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
			redo_prop.setValue(controller.cantRedo)
			grid = Some(createGrid(event.field))
			stage.setScene(makeGameScene(grid.get))
		})
	}

	private def updateGrid(field: FieldInterface): Unit = {
		grid.get.getChildren.forEach(node => {
			val button = node.asInstanceOf[javafx.scene.control.Button]
			val x = javafx.scene.layout.GridPane.getColumnIndex(button)
			val y = javafx.scene.layout.GridPane.getRowIndex(button)
			val cell = field.getCell(x, y).get

			if cell.isFlagged then button.setGraphic(new ImageView(images.get("flagged")))
			else if cell.isRevealed then
				button.getStyleClass.add("revealed")
				if cell.isBomb then button.setGraphic(new ImageView(images.get("bomb")))
				else if cell.nearbyBombs != 0 then button.setGraphic(new ImageView(images.get(cell.nearbyBombs.toString)))
				else button.setGraphic(new ImageView(images.get("revealed")))
			else
				button.getStyleClass.remove("revealed")
				button.setGraphic(new ImageView(images.get("unrevealed")))
		})
	}

	private def createGrid(field: FieldInterface): GridPane = {
		println(s"creating grid with width ${field.dimension._1} and height ${field.dimension._2}")
		val grid = new GridPane() {
			id = "game-cell-grid"
		}

		val (width, height) = field.dimension
		for (ix <- 0 until width) {
			for (iy <- 0 until height) {
				val cell = field.getCell(ix, iy).get
				grid.add(new Button("", new ImageView(images.get("unrevealed"))) {
					styleClass = Seq("cell")
					onMouseClicked = if (cell.isRevealed) null else
						e => {
							if (e.getButton == javafx.scene.input.MouseButton.PRIMARY) {
								controller.reveal(ix, iy)
							} else if (e.getButton == javafx.scene.input.MouseButton.SECONDARY) {
								controller.flag(ix, iy)
							}
						}
				}, ix, iy)
			}
		}
		grid
	}
}