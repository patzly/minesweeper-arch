# Minesweeper
[![Scala CI](https://github.com/bafto/minesweeper/actions/workflows/scala.yml/badge.svg)](https://github.com/bafto/minesweeper/actions/workflows/scala.yml) [![Coverage Status](https://coveralls.io/repos/github/bafto/minesweeper/badge.svg)](https://coveralls.io/github/bafto/minesweeper)

Software Engineering Projekt an der HTWG im 3. Semester

## Spielprinzip
Das Ziel von Minesweeper ist es in einem Zufällig generiertem Mienenfeld alle freien Felder mit links klick auzudecken, ohne auf eine Mine zu klicken.
Die erste Zelle, die man aufdeckt, ist immer frei.

Freie Zellen, die mindestens eine Miene als Nachbar besitzen, haben eine Zahl, welche die Anzahl der benachbarten Mienen anzeigt.

Wenn eine freie Zelle aufdedeckt wird, die keine Minen als Nachbarn hat, werden die Nachbarn der Miene ebenfalls aufgedeckt.

Man kann mit rechts klick eine Flagge setzten, falls man vermutet, dass sich dort eine Mine befindet und sich diese Position merken will.

Sobald alle freien felder aufgedeckt wurden, ist das Spiel gewonnen. Wenn man auf eine Mine klickt, ist das Spiel verloren.
Von dort aus kann man ein neues Spiel anfangen oder zum Menü zurückkehren.
