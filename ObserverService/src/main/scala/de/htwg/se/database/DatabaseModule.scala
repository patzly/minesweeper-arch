package de.htwg.se.database

import de.htwg.se.database.slickimpl.SlickClientDao
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object DatabaseModule {
  def init(dbPath: String)(implicit ec: ExecutionContext): ClientDao = {
    val db = Database.forURL(s"jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
    val setup = Clients.table.schema.createIfNotExists
    db.run(setup).onComplete({
      case Success(_) => println(s"Database $dbPath initialized successfully.")
      case Failure(exception) => println(s"Failed to initialize database $dbPath: ${exception.getMessage}")
    })
    new SlickClientDao(db)
  }
}