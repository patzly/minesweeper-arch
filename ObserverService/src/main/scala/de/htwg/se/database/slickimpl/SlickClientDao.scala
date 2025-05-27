package de.htwg.se.database.slickimpl

import de.htwg.se.database.{ClientDao, ClientEntity, Clients}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class SlickClientDao(db: Database)(implicit ec: ExecutionContext) extends ClientDao {
  private val table = Clients.table

  override def insert(client: String): Future[Unit] =
    db.run(table += ClientEntity(client)).map(_ => ())

  override def delete(client: String): Future[Unit] =
    db.run(table.filter(_.clientUrl === client).delete).map(_ => ())

  override def list(): Future[Set[String]] =
    db.run(table.result).map(_.map(_.clientUrl).toSet)
}
