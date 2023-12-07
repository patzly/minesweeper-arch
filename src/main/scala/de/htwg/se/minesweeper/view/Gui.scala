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
import javafx.beans.property.SimpleIntegerProperty
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.scene.image.{Image, ImageView}


class Gui(controller: FieldController) extends JFXApp3 with Observer[Event] with EventVisitor {
    controller.addObserver(this)
    private var setup_field: Option[Field] = None

    private var grid: Option[GridPane] = None
    private var my_scene: Option[Scene] = None

    override def start(): Unit = {
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

    private def makeScene(gridPane: GridPane): Scene = {
        new Scene {
            fill = Color.rgb(38, 38, 38)
            content = new BorderPane() {
                top = new Text {
                    text <== new SimpleIntegerProperty(controller.undos).asString("Undos: %d")
                }
                center = gridPane
                bottom = new VBox {
                    alignment = Pos.Center
                    padding = Insets(20)
                    children = Seq(new HBox {
                        alignment = Pos.Center
                        spacing = 100
                        children = Seq(
                            new Button("Zum Menü"),
                            new Button("Undo") {
                                onMouseClicked = e => controller.undo()
                            },
                            new Button("Redo") {
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
            case _ => Platform.runLater(() => {
                e.accept(this)
            })
        }
    }

    override def visitExit(event: ExitEvent): Unit = {
        // close the gui
        stage.close()
        System.exit(0)
    }

    private def getEndScreen(str: String): Node = new VBox(
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
        alignment = Pos.Center
        padding = Insets(10)
        spacing = 10
    }

    override def visitLost(event: LostEvent): Unit = {
        // show the lost screen
        // and a retry button
        my_scene.get.content = getEndScreen("You lost!")
    }

    override def visitWon(event: WonEvent): Unit = {
        // show the won screen
        // and a retry button
        my_scene.get.content = getEndScreen("You won!")
    }

    override def visitFieldUpdated(event: FieldUpdatedEvent): Unit = {
        // update the gui
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

            if cell.isFlagged then button.setGraphic(new ImageView(new Image("file:img/flagged.png")))
            else if cell.isRevealed then
                if cell.isBomb then button.setGraphic(new ImageView(new Image("file:img/bomb.png")))
                else if cell.nearbyBombs != 0 then button.setGraphic(new ImageView(new Image(s"file:img/${cell.nearbyBombs}.png")))
                else button.setGraphic(new ImageView(new Image("file:img/revealed.png")))
        })
    }

    private def createGrid(field: Field): GridPane = {
        val grid = new GridPane()

        for (ix <- 0 until field.dimension._1) {
            for (iy <- 0 until field.dimension._2) {
                val cell = field.getCell(ix, iy).get
                grid.add(new Button("", new ImageView(new Image("file:img/unrevealed.png"))) {
                    padding = Insets(-1)
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