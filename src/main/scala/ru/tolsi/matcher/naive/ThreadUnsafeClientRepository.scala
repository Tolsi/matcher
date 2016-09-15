package ru.tolsi.matcher.naive

import scala.concurrent.{ExecutionContext, Future}
import ru.tolsi.matcher.{Client, ClientRepository}

private[naive] object ThreadUnsafeClientRepository {
  def apply(clients: Seq[Client[Long]]): ThreadUnsafeClientRepository = new ThreadUnsafeClientRepository(clients.map(
    c => c.id -> c).toMap)
}
private[naive] class ThreadUnsafeClientRepository(private[naive] val map: Map[String, Client[Long]] = Map.empty)
  extends ClientRepository[Long] {
  override def get(id: String)(implicit ec: ExecutionContext): Future[Option[Client[Long]]] =
    Future.successful(map.get(id))
  override def getAll(implicit ec: ExecutionContext): Future[Iterable[Client[Long]]] =
    Future.successful(map.values)
}
