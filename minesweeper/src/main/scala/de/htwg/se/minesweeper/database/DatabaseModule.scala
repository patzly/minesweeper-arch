package de.htwg.se.minesweeper.database

import de.htwg.se.minesweeper.database.slickimpl.SlickGameStateDao
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object DatabaseModule {
  def init(dbPath: String)(implicit ec: ExecutionContext): GameStateDao = {
    val db = Database.forURL(s"jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
    val setup = GameStates.table.schema.createIfNotExists
    db.run(setup).onComplete({
      case Success(_) => println(s"Database $dbPath initialized successfully.")
      case Failure(exception) => println(s"Failed to initialize database $dbPath: ${exception.getMessage}")
    })
    new SlickGameStateDao(db)
  }
}