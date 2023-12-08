package de.htwg.se.minesweeper.controller.baseController

import scala.util.Try
import de.htwg.se.minesweeper.controller._

private trait Command {
    def execute(): Try[Unit]
    def undo(): Unit
    def redo(): Try[Unit]
}

private class RevealCommand(controller: BaseController, x: Int, y: Int) extends Command {
    private val field = controller.getField
    override def execute(): Try[Unit] = controller.state.reveal(x, y)
    override def undo(): Unit = {
        controller.field = field
        controller.notifyObservers(FieldUpdatedEvent(field))
    }
    override def redo(): Try[Unit] = controller.state.reveal(x, y)
}

private class FlagCommand(controller: BaseController, x: Int, y: Int) extends Command {
    override def execute(): Try[Unit] = controller.flag_impl(x, y)
    override def undo(): Unit = controller.flag_impl(x, y)
    override def redo(): Try[Unit] = controller.flag_impl(x, y)
}
