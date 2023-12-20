package de.htwg.se.minesweeper.controller

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface

trait Event {
    def accept(visitor: EventVisitor): Unit
}

case class SetupEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitSetup(this)
}

case class StartGameEvent(field: FieldInterface) extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitStartGame(this)
}

case class FieldUpdatedEvent(field: FieldInterface) extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitFieldUpdated(this)
}

case class WonEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitWon(this)
}

case class LostEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitLost(this)
}

case class ExitEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitExit(this)
}

trait EventVisitor {
    def visitSetup(event: SetupEvent): Unit
    def visitStartGame(event: StartGameEvent): Unit
    def visitFieldUpdated(event: FieldUpdatedEvent): Unit
    def visitWon(event: WonEvent): Unit
    def visitLost(event: LostEvent): Unit
    def visitExit(event: ExitEvent): Unit
}
