package de.htwg.se.minesweeper.view

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import scalafx.application.Platform
import scalafx.scene.layout.GridPane
import scalafx.scene.Node
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.controller._
import scalafx.scene.control.Button
import de.htwg.se.minesweeper.observer._
import de.htwg.se.minesweeper.model.Field
import de.htwg.se.minesweeper.observer.Observer

class Gui(controller: FieldController) extends JFXApp3 with Observer[Event] with EventVisitor {
    controller.addObserver(this)
    private var setup_field: Option[Field] = None

    private var grid: GridPane = null
    private var my_scene: Scene = null

    override def start() = {
        grid = createGrid(setup_field.get)
        my_scene = new Scene {
            fill = Color.rgb(38, 38, 38)
            content = grid
        }
        stage = new JFXApp3.PrimaryStage {
            //    initStyle(StageStyle.Unified)
            title = "Minesweeper"
            scene = my_scene
            onCloseRequest = e => {
                controller.exit()
            }
        }
    }

    override def update(e: Event): Unit = {
        e match {
            case SetupEvent(field) => e.accept(this)
            case _ => Platform.runLater(() => {
                e.accept(this)
            })
        }
    }

    override def visitExit(event: ExitEvent): Unit = {
        // close the gui
        stage.close()
    }

    private def getEndScreen(str: String): Node = new scalafx.scene.layout.VBox(
        new Text {
            text = str
            style = "-fx-font-size: 48;"
            fill = Color.White
        },
        new Button {
            text = "Retry"
            style = "-fx-font-size: 24;"
            onMouseClicked = e => {
                controller.setup()
            }
        }
    ) {
        padding = Insets(10)
        spacing = 10
    }

    override def visitLost(event: LostEvent): Unit = {
        // show the lost screen
        // and a retry button
        my_scene.content = getEndScreen("You lost!")
    }

    override def visitWon(event: WonEvent): Unit = {
        // show the won screen
        // and a retry button
        my_scene.content = getEndScreen("You won!")
    }

    override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
        // update the gui
        updateGrid(event.field)
    }

    override def visitSetup(event: SetupEvent): Unit = {
        setup_field match {
            case None => setup_field = Some(event.field)
            case Some(_) => Platform.runLater({
                grid = createGrid(event.field)
                my_scene.content = grid
            })
        }
    }

    private def updateGrid(field: Field): Unit = {
        grid.getChildren().forEach(node => {
            val button = node.asInstanceOf[javafx.scene.control.Button]
            val x = javafx.scene.layout.GridPane.getColumnIndex(button)
            val y = javafx.scene.layout.GridPane.getRowIndex(button)
            val cell = field.getCell(x, y).get
            button.setText(cell.toString)
        })
    }

    private def createGrid(field: Field): GridPane = {
        var grid = new GridPane()
        for (ix <- 0 until field.dimension._1) {
            for (iy <- 0 until field.dimension._2) {
                val cell = field.getCell(ix, iy).get
                grid.add(new Button {
                    text = cell.toString
                    // add a monospaced font with size 16
                    style = "-fx-font-family: monospace; -fx-font-size: 25;"
                    onMouseClicked = if (cell.isRevealed) null else
                        e => {
                        if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                            controller.reveal(ix, iy)
                        } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                            controller.flag(ix, iy)
                        }
                    }
                }, ix, iy)
            }
        }
        grid
    }
}