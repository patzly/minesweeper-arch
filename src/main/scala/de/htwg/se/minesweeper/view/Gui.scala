package de.htwg.se.minesweeper.view

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.layout.*
import scalafx.scene.paint.Color.*
import scalafx.scene.paint.*
import scalafx.scene.text.Text
import scalafx.application.Platform
import scalafx.scene.Node
import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.controller.*
import scalafx.scene.control.*
import de.htwg.se.minesweeper.observer.Observer
import javafx.beans.property.{SimpleBooleanProperty, SimpleIntegerProperty, SimpleStringProperty}
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.scene.image.{Image, ImageView}

import scala.util.{Failure, Success, Try}

class Gui(controller: FieldController) extends JFXApp3 with Observer[Event] with EventVisitor {
	controller.addObserver(this)
	private var setup_field: Option[Field] = None

	private var grid: Option[GridPane] = None
	private var my_scene: Option[Scene] = None

	private var images: Option[Map[String, Image]] = None
	private val undo_prop = new SimpleIntegerProperty(controller.undos)
	private val redo_prop = new SimpleBooleanProperty(controller.cantRedo)

	private val end_screen_visible = SimpleBooleanProperty(false)
	private val end_screen_text = SimpleStringProperty("")

	override def start(): Unit = {
		images = createImages() match {
			case Success(value) => Some(value)
			case Failure(e) => throw new Exception("Could not load images!")
		}
		grid = Some(createGrid(setup_field.get))

		my_scene = Some(makeScene(grid.get))

		stage = new JFXApp3.PrimaryStage {
			//    initStyle(StageStyle.Unified)
			title = "Minesweeper"
			scene = my_scene.get
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

	private def makeScene(gridPane: GridPane): Scene = {
		new Scene {
			stylesheets = List(getClass.getResource("/style.css").toExternalForm)
			fill = Color.rgb(38, 38, 38)
			content = new BorderPane() {
				// white text
				top = new Text {
					id = "undo-text"
					text <== undo_prop.asString("Undos: %d")
				}
				center = new StackPane {
					children = Seq(
						gridPane,
						new HBox(
							new VBox(
								new Text {
									text <== end_screen_text
									id = "end-text"
								},
								new Button {
									text = "Retry"
									// black text
									style = "-fx-font-size: 24; -fx-fill: black;"
									onMouseClicked = e => {
										end_screen_visible.setValue(false)
										controller.setup()
									}
								},
							) {
								alignment = Pos.Center
								spacing = 10
								fillWidth = true
							}
						) {
							id = "end-screen"
							visible <== end_screen_visible
							alignment = Pos.Center
							spacing = 10
							fillHeight = true
						}
					)
				}
				bottom = new VBox {
					alignment = Pos.Center
					padding = Insets(20)
					children = Seq(new HBox {
						alignment = Pos.Center
						spacing = 100
						children = Seq(
							new Button("Zum Menü"),
							new Button("Undo") {
								disable <== end_screen_visible.or(undo_prop.isEqualTo(0))
								onMouseClicked = e => controller.undo()
							},
							new Button("Redo") {
								disable <== end_screen_visible.or(redo_prop)
								onMouseClicked = e => controller.redo()
							})
					})
				}
				padding = Insets(50)
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
		Platform.runLater(undo_prop.setValue(controller.undos))
		Platform.runLater(redo_prop.setValue(controller.cantRedo))
		updateGrid(event.field)
	}

	override def visitSetup(event: SetupEvent): Unit = {
		setup_field match {
			case None => setup_field = Some(event.field)
			case Some(_) => Platform.runLater({
				grid = Some(createGrid(event.field))
				val newScene = makeScene(grid.get)
				my_scene.get.content.setAll(newScene.content)
			})
		}
	}

	private def updateGrid(field: Field): Unit = {
		grid.get.getChildren.forEach(node => {
			val button = node.asInstanceOf[javafx.scene.control.Button]
			val x = javafx.scene.layout.GridPane.getColumnIndex(button)
			val y = javafx.scene.layout.GridPane.getRowIndex(button)
			val cell = field.getCell(x, y).get

			if cell.isFlagged then button.setGraphic(new ImageView(images.get("flagged")))
			else if cell.isRevealed then
				if cell.isBomb then button.setGraphic(new ImageView(images.get("bomb")))
				else if cell.nearbyBombs != 0 then button.setGraphic(new ImageView(images.get(cell.nearbyBombs.toString)))
				else button.setGraphic(new ImageView(images.get("revealed")))
			else button.setGraphic(new ImageView(images.get("unrevealed")))
		})
	}

	private def createGrid(field: Field): GridPane = {
		val grid = new GridPane()

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