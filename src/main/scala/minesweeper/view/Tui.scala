package minesweeper.view

class Tui() {
    def processLine(line: String): Unit = {
        line match {
            case "q" => System.exit(0)
            case _ => {
                line.split(" ").toList match {
                    case xc :: yc :: _ => {
                        val (x, y) = (xc.toIntOption, yc.toIntOption) match {
                            case (Some(x), Some(y)) => (x, y)
                            case _ => {
                                System.err.println("Invalid input")
                                return ()
                            }
                        }
                        println(s"Selected ($x, $y)")
                    }
                    case _ => System.err.println("Invalid input")
                }
            }
        }
    }
}