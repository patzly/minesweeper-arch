package minesweeper.observer

trait Observer[E] {
    def update(e: E): Unit
}

class Observable[E] {
    private var subscribers: Vector[Observer[E]] = Vector()
    def addObserver(o: Observer[E]) = subscribers = subscribers :+ o
    def removeObserver(o: Observer[E]) = subscribers = subscribers.filterNot(_ == o)
    def notifyObservers(e: E) = subscribers.foreach(_.update(e))
}