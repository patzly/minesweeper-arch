package de.htwg.se.minesweeper.controller

import de.htwg.se.minesweeper.model.fieldComponent.FieldInterface

// Events are used to communicate between the controller and the views
// on each action an event is created and passed to the views
// the views can react to the event by using the visitor pattern
trait Event {
    def accept(visitor: EventVisitor): Unit
}

// sent when we enter the main menu
case class SetupEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitSetup(this)
}

// sent when we start a new game
case class StartGameEvent(field: FieldInterface) extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitStartGame(this)
}

// sent when the field changed in any way (reveal, flag, undo, redo)
case class FieldUpdatedEvent(field: FieldInterface) extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitFieldUpdated(this)
}

// sent when the game is won
case class WonEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitWon(this)
}

// sent when the game is lost
case class LostEvent() extends Event {
    override def accept(visitor: EventVisitor): Unit = visitor.visitLost(this)
}

// sent when the user quits the game
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
