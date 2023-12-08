package de.htwg.se.minesweeper.controller.spieController

import de.htwg.se.minesweeper.controller._
import de.htwg.se.minesweeper.controller.baseController.BaseController
import de.htwg.se.minesweeper.observer.Observable
import de.htwg.se.minesweeper.model.fieldComponent.FieldFactory

class SpieController(base_undos: Int, val field_factory: FieldFactory) extends BaseController(base_undos, field_factory) with ControllerInterface {
    var didWin = false
    var didLose = false
    var didExit = false
    
    override def setup(): Unit = {
        didWin = false
        didLose = false
        didExit = false
        super.setup()
    }

    override def exit(): Unit = {
        didExit = true
        super.exit()
    }
}