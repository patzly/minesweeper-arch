package minesweeper.controller

import scala.util.Try

private[controller] trait Command {
    def execute(): Try[Unit]
    def undo(): Unit
    def redo(): Try[Unit]
}

private[controller] class RevealCommand(controller: FieldController, x: Int, y: Int) extends Command {
    private val field = controller.field
    override def execute(): Try[Unit] = controller.state.reveal(x, y)
    override def undo(): Unit = {
        controller.field = field
        controller.notifyObservers(FieldUpdatedEvent(field))
    }
    override def redo(): Try[Unit] = controller.state.reveal(x, y)
}

private class FlagCommand(controller: FieldController, x: Int, y: Int) extends Command {
    override def execute(): Try[Unit] = controller.flag_impl(x, y)
    override def undo(): Unit = controller.flag_impl(x, y)
    override def redo(): Try[Unit] = controller.flag_impl(x, y)
}
