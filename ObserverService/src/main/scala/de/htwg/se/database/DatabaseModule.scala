package de.htwg.se.database

import de.htwg.se.database.mongoimpl.MongoClientDao
import de.htwg.se.database.slickimpl.SlickClientDao
import org.mongodb.scala.{MongoClient, MongoDatabase}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object DatabaseModule {
  def initSlick(dbPath: String)(implicit ec: ExecutionContext): ClientDao = {
    val db = Database.forURL(s"jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
    val setup = Clients.table.schema.createIfNotExists
    db.run(setup).onComplete({
      case Success(_) => println(s"Database $dbPath initialized successfully.")
      case Failure(exception) => println(s"Failed to initialize database $dbPath: ${exception.getMessage}")
    })
    new SlickClientDao(db)
  }

  def initMongo(connectionString: String, dbName: String)(implicit ec: ExecutionContext): ClientDao = {
    val client: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = client.getDatabase(dbName)
    new MongoClientDao(database)
  }
}