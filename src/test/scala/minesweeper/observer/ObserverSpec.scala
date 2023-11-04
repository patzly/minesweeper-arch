package minesweeper.observer

import minesweeper.controller._
import minesweeper.model._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.lang.IndexOutOfBoundsException
import minesweeper.observer.Observer

class TestObserver extends Observer[Int] {
    var i: Int = 0
    override def update(i: Int): Unit = {
        this.i = i
    }
}

class TestObservable extends Observable[Int] {}

class ObservableSpec extends AnyWordSpec {
    "An Observable" when {
        "it has a single observer" should {
            val observable = TestObservable()
            var observer = TestObserver()
            observable.addObserver(observer)

            "without notification" in {
                observer.i shouldBe(0)
            }
            "notify the observer" in {
                observable.notifyObservers(1)
                observer.i shouldBe(1)
            }
            "remove the observer" in {
                observable.removeObserver(observer)
                observable.notifyObservers(2)
                observer.i shouldBe(1)
            }
        }
        "it has multiple observers" should {
            val observable = TestObservable()
            var observer1 = TestObserver()
            var observer2 = TestObserver()
            observable.addObserver(observer1)
            observable.addObserver(observer2)

            "notify the observers" in {
                observable.notifyObservers(1)
                observer1.i shouldBe(1)
                observer2.i shouldBe(1)
            }
            "remove the observer" in {
                observable.removeObserver(observer1)
                observable.notifyObservers(2)
                observer1.i shouldBe(1)
                observer2.i shouldBe(2)
            }
        }
    }
}