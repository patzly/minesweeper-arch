package de.htwg.se.minesweeper

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.google.inject.{Guice, Injector}
import de.htwg.se.minesweeper.controller.ControllerInterface
import de.htwg.se.minesweeper.controller.baseController.BaseController
import de.htwg.se.minesweeper.model.fieldComponent.FieldFactory
import de.htwg.se.minesweeper.model.fieldComponent.field.RandomFieldFactory

class MinesweeperModuleSpec extends AnyWordSpec with Matchers {
	"A MinesweeperModule" should {
		"configure Guice bindings" in {
			val injector: Injector = Guice.createInjector(new MinesweeperModule)

			// Test the binding for FieldFactory
			val fieldFactory: FieldFactory = injector.getInstance(classOf[FieldFactory])
			fieldFactory shouldBe a[RandomFieldFactory]

			// Test the binding for ControllerInterface
			val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])
			controller shouldBe a[BaseController]
		}
	}
}
