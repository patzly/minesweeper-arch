package de.htwg.se.database

import scala.concurrent.Future

trait ClientDao {
  def insert(client: String): Future[Unit]
  def delete(client: String): Future[Unit]
  def list(): Future[Set[String]]
}
