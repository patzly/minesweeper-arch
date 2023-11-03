package minesweeper

import scala.util.Random
import model.*
import view.Tui
import view.TUIState
import controller.FieldController

@main def main(): Unit =
	val width = 16
	val height = 16
	val bomb_chance = 0.15f
	val rand = new Random()

	val controller = FieldController(height, width, Field.getRandBombGen(rand, bomb_chance))
	val tui = Tui(controller)

	println(tui)
	while processInput(tui) do { }

def processInput(tui: Tui) : Boolean =
	val state: TUIState = tui.processLine(scala.io.StdIn.readLine())

	println(tui)
	state match
		case TUIState.Continue => true
		case TUIState.Won =>
			println("You Won!")
			false
		case TUIState.Lost =>
			println("You Lost!")
			false
		case TUIState.Invalid(msg) =>
			println(msg)
			true
		case TUIState.Exit => false
