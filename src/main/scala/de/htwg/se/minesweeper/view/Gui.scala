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

import scala.util.{Failure, Success, Try}

class Gui(controller: ControllerInterface) extends JFXApp3 with Observer[Event] with EventVisitor {
	controller.addObserver(this)
	private var setup_field: Option[FieldInterface] = None

	private var grid: Option[GridPane] = None

	private var images: Option[Map[String, Image]] = None
	private val undo_prop = IntegerProperty(controller.getUndos)
	private val redo_prop = BooleanProperty(controller.cantRedo)

	private val end_screen_visible = BooleanProperty(false)
	private val end_screen_text = StringProperty("")

	private val stylesheet = getClass.getResource("/style.css").toExternalForm
	private val background_color = Color.rgb(38, 38, 38)

	override def start(): Unit = {
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
		val controls = new GridPane() { id = "controls-pane" }
		controls.add(new Text("Breite") {styleClass = Seq("white")}, 0, 0)
		controls.add(new Text("Höhe") {styleClass = Seq("white")}, 0, 1)
		controls.add(new Text("Bomben Verteilung") {styleClass = Seq("white")}, 0, 2)
		controls.add(new Spinner(1, 16, 8) {
			maxWidth = 100
		}, 1, 0)
		controls.add(new Spinner(1, 16, 8) {
			maxWidth = 100
		}, 1, 1)
		controls.add(new Spinner[Double](0.0, 1.0, 0.15, 0.1) {
			maxWidth = 100
		}, 1, 2)

		new Scene {
			stylesheets = List(stylesheet)
			fill = background_color
			content = new BorderPane {
				top = new HBox(new Text("Minesweeper") {
					styleClass = Seq("h1", "text-center", "bold", "white")
				}) {
					alignment = Pos.Center
				}
				center = new FlowPane {
					children = Seq(
						controls,
						new Button("Spielen") {
							onMouseClicked = e => controller.setup()
						}
					)
					id = "main-menu-bottom"
				}
				bottom = new FlowPane {
					children = Seq(
						new Text("Software Engineering Projekt WS23/24") {
							styleClass = Seq("h2", "text-center", "bold", "white")
						}, new Text("Leon Gies und Hendrik Ziegler") {
							styleClass = Seq("h3", "text-center", "bold", "white")
						}
					)
					id = "main-menu-bottom"
				}
				padding = Insets(50)
			}
		}
	}

	private def makeGameScene(gridPane: GridPane): Scene = {
		new Scene {
			stylesheets = List(stylesheet)
			fill = background_color
			content = new BorderPane() {
				padding = Insets(50)
				top = new Text {
					styleClass = Seq("h2", "text-center", "bold", "white")
					text <== undo_prop.asString("Undos: %d")
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
									id = "retry-btn"
									onMouseClicked = e => {
										end_screen_visible.setValue(false)
										controller.setup()
									}
								}
							)
							id = "end-screen"
							visible <== end_screen_visible
						}
					)
				}
				bottom = new FlowPane {
					id = "bottom"
					children = Seq(
						new Button("Zum Menü") {
							onMouseClicked = e => stage.setScene(makeMainScene())
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
			case SetupEvent(field) => e.accept(this)
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
		setup_field match {
			case None => setup_field = Some(event.field)
			case Some(_) => Platform.runLater({
				grid = Some(createGrid(event.field))
				stage.setScene(makeGameScene(grid.get))
			})
		}
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
		val grid = new GridPane() {
			id = "cell-grid"
		}

		for (ix <- 0 until field.dimension._1) {
			for (iy <- 0 until field.dimension._2) {
				val cell = field.getCell(ix, iy).get
				grid.add(new Button("", new ImageView(images.get("unrevealed"))) {
					styleClass = Seq("cell")
					padding = Insets(0)
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