package de.htwg.se.database

import slick.lifted.{ProvenShape, TableQuery, Tag}
import slick.jdbc.SQLiteProfile.api.*

final case class ClientEntity(clientUrl: String)

private class ClientTable(tag: Tag) extends Table[ClientEntity](tag, "clients") {
  def clientUrl: Rep[String] = column[String]("client_url", O.PrimaryKey)
  def * = Tuple1(clientUrl) <> ({
    case Tuple1(url) => ClientEntity(url)
  },
    client => Some(Tuple1(client.clientUrl))
  )
}

object Clients {
  val table = TableQuery[ClientTable]
}