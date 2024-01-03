package de.htwg.se.minesweeper.view

import de.htwg.se.minesweeper.controller.*
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{BooleanProperty, IntegerProperty, StringProperty}
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.scene.{Node, Scene}
import scalafx.scene.text.Text
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane as JGridPane
import scala.util.{Failure, Success}

case class GameScene(controller: ControllerInterface) extends Scene {
	private val undo_prop = IntegerProperty(controller.getGameState.undos)
	private val cant_undo_prop = BooleanProperty(controller.getGameState.cantUndo)
	private val redo_prop = BooleanProperty(controller.getGameState.cantRedo)

	private val end_screen_visible = BooleanProperty(false)
	private val end_screen_text = StringProperty("")

	private val time_prop = IntegerProperty(0)
	private val t = new java.util.Timer()
	private val task = new java.util.TimerTask {
		def run(): Unit = time_prop.value = time_prop.value + 1
	}
	t.schedule(task, 1000L, 1000L)

	// create grid
	private val grid = new GridPane {
		id = "game-cell-grid"
	}

	private val (gridWidth, gridHeight) = controller.getGameState.field.dimension
	for (ix <- 0 until gridWidth) {
		for (iy <- 0 until gridHeight) {
			grid.add(new Button {
				styleClass = Seq("cell")
				onMouseClicked = e => e.getButton match {
					case MouseButton.PRIMARY => controller.reveal(ix, iy) match {
						case Success(_) => ()
						case Failure(exception) => println(exception.getMessage)
					}
					case MouseButton.SECONDARY => controller.flag(ix, iy) match {
						case Success(_) => ()
						case Failure(exception) => println(exception.getMessage)
					}
					case _ => ()
				}
				prefWidth <== Bindings.min(grid.widthProperty().divide(gridWidth.doubleValue), grid.heightProperty().divide(gridHeight.doubleValue))
				prefHeight <== Bindings.min(grid.widthProperty().divide(gridWidth.doubleValue), grid.heightProperty().divide(gridHeight.doubleValue))
			}, ix, iy)
		}
	}

	stylesheets = List(getClass.getResource("/style.css").toExternalForm)
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
								val (width, height) = controller.getGameState.field.dimension
								controller.startGame(width, height, controller.getGameState.bombChance, controller.getGameState.maxUndos)
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

	def update(event: FieldUpdatedEvent): Unit = {
		undo_prop.setValue(controller.getGameState.undos)
		cant_undo_prop.setValue(controller.getGameState.cantUndo)
		redo_prop.setValue(controller.getGameState.cantRedo)

		// update the grid
		grid.getChildren.forEach(button => {
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

	def showLossScreen(): Unit = {
		end_screen_visible.setValue(true)
		end_screen_text.setValue("You lost!")
	}

	def showWinScreen(): Unit = {
		end_screen_visible.setValue(true)
		end_screen_text.setValue("You won!")
	}
}
