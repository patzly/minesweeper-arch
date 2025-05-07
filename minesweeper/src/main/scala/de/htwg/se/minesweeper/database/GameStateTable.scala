package de.htwg.se.minesweeper.database

import slick.lifted.{ProvenShape, TableQuery, Tag}
import slick.jdbc.SQLiteProfile.api.*

case class GameStateEntity(id: String = "", json: String)

class GameStateTable(tag: Tag) extends Table[GameStateEntity](tag, "game_state") {
  def id = column[String]("id", O.PrimaryKey)
  def json = column[String]("json")

  def * = (id, json) <> ( {
    case (id, json) => GameStateEntity(id, json)
  },
    GameStateEntity.unapply
  )
}

object GameStates {
  val table = TableQuery[GameStateTable]
}
