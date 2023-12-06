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

class Gui(controller: FieldController) extends JFXApp3 with Observer[Event] with EventVisitor {
    controller.addObserver(this)
    private var isReady = false

    def ready(): Boolean = isReady

    override def start() = {
        stage = new JFXApp3.PrimaryStage {
            //    initStyle(StageStyle.Unified)
            title = "ScalaFX Hello World"
            scene = new Scene {
                fill = Color.rgb(38, 38, 38)
            }
        }
        isReady = true
    }

    override def stopApp(): Unit = {
        controller.exit()
    }

    override def update(e: Event): Unit = {
        Platform.runLater(() => {
            e.accept(this)
        })
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

    private def createGrid(field: Field): GridPane = {
        var grid = new GridPane()
        for (ix <- 0 until field.dimension._1) {
            for (iy <- 0 until field.dimension._2) {
                grid.add(new Text {
                    text = field.getCell(ix, iy).get.toString
                    // add a monospaced font with size 16
                    style = "-fx-font-family: monospace; -fx-font-size: 25;"
                    fill = new LinearGradient(
                        endX = 0,
                        stops = Stops(Red, DarkRed)
                    )
                }, ix, iy)
            }
        }
        grid
    }
}