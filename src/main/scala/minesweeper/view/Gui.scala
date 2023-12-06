package minesweeper.view

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import minesweeper.observer._
import minesweeper.controller._
import scalafx.application.Platform
import scalafx.scene.layout.GridPane
import scalafx.scene.Node
import minesweeper.model.Field
import scalafx.scene.control.Button

class Gui(controller: FieldController) extends JFXApp3 with Observer[Event] with EventVisitor {
    controller.addObserver(this)
    private var setup_field: Field = null

    override def start() = {
        stage = new JFXApp3.PrimaryStage {
            //    initStyle(StageStyle.Unified)
            title = "ScalaFX Hello World"
            scene = new Scene {
                fill = Color.rgb(38, 38, 38)
                content = createGrid(setup_field)
            }
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

    override def visitLost(event: LostEvent): Unit = {
        // show the lost screen
        throw new NotImplementedError()
    }

    override def visitWon(event: WonEvent): Unit = {
        // show the won screen
        throw new NotImplementedError()
    }

    override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
        // update the gui
        stage = new JFXApp3.PrimaryStage {
            //    initStyle(StageStyle.Unified)
            title = "ScalaFX Hello World"
            scene = new Scene {
                fill = Color.rgb(38, 38, 38)
                content = createGrid(event.field)
            }
        }
    }

    override def visitSetup(event: SetupEvent): Unit = {
        setup_field = event.field
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