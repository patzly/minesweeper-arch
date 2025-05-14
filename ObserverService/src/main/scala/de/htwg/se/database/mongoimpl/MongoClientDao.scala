package de.htwg.se.database.mongoimpl

import de.htwg.se.database.ClientDao
import org.mongodb.scala.*
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Projections.*

import scala.concurrent.{ExecutionContext, Future}

class MongoClientDao(database: MongoDatabase)(implicit ec: ExecutionContext) extends ClientDao {

  private val collection: MongoCollection[Document] = database.getCollection("clients")

  override def insert(client: String): Future[Unit] = {
    val doc = Document("client_url" -> client)
    collection.insertOne(doc).toFuture().map(_ => ())
  }

  override def delete(client: String): Future[Unit] = {
    collection.deleteOne(equal("client_url", client)).toFuture().map(_ => ())
  }

  override def list(): Future[Set[String]] = {
    collection.find()
      .projection(fields(include("client_url"), excludeId()))
      .toFuture()
      .map(docs => docs.map(_.getString("client_url")).toSet)
  }
}