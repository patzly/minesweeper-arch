package minesweeper.observer

trait Observer[E] {
    def update(e: E): Unit
}

class Observable[E] {
    private var subscribers: Vector[Observer[E]] = Vector()
    def addObserver(o: Observer[E]): Unit = subscribers = subscribers :+ o
    def removeObserver(o: Observer[E]): Unit = subscribers = subscribers.filterNot(_ == o)
    def notifyObservers(e: E): Unit = subscribers.foreach(_.update(e))
}