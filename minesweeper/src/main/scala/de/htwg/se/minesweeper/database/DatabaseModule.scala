package de.htwg.se.minesweeper.database

import de.htwg.se.minesweeper.database.mongoimpl.MongoGameStateDao
import de.htwg.se.minesweeper.database.slickimpl.SlickGameStateDao
import org.mongodb.scala.{MongoClient, MongoDatabase}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object DatabaseModule {
  
  def initSlick(dbPath: String)(implicit ec: ExecutionContext): GameStateDao = {
    val db = Database.forURL(s"jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
    val setup = GameStates.table.schema.createIfNotExists
    db.run(setup).onComplete({
      case Success(_) => println(s"Database $dbPath initialized successfully.")
      case Failure(exception) => println(s"Failed to initialize database $dbPath: ${exception.getMessage}")
    })
    new SlickGameStateDao(db)
  }

  def initMongo(connectionString: String, dbName: String)(implicit ec: ExecutionContext): GameStateDao = {
    val client: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = client.getDatabase(dbName)
    new MongoGameStateDao(database)
  }
}