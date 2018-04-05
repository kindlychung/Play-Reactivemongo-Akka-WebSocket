package repository

import javax.inject.Inject
import reactivemongo.api._
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.scaladsl.Source
import models._
import play.api.Logger
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, Macros}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success, Try}

trait UserRepositoryT {
  import reactivemongo.akkastream._
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  implicit def userWriter = Macros.writer[User]
  implicit def userReader = Macros.reader[User]
  implicit def ec: ExecutionContext
  val uri = "mongodb://localhost:27017/playwebsocketdemo"
  val dbname = "playwebsocketdemo"
  val collectionName = "users"
  val driver = MongoDriver()
  val parsedUri: Try[MongoConnection.ParsedURI] = MongoConnection.parseURI(uri)
  val connection: Try[MongoConnection] = parsedUri.map(driver.connection)
  lazy val db: Future[DefaultDB] = connection match {
    case Success(conn) =>
      println(s"==== Connection: $conn")
      conn.database(dbname)
    case Failure(e) => throw new Exception(s"failed to connect to mongodb $uri")
  }
  lazy val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  def create(user: User): Future[WriteResult] = {
    collection.flatMap(_.insert(user))
  }

  private def createCursor: Future[AkkaStreamCursor[User]] = {
    collection.map(_.find(BSONDocument())
      .options(QueryOpts().tailable.awaitData)
      .cursor[User]())
  }

  def listenUserCollection(f: User => Unit): Unit = {
    val c = createCursor
    c.onComplete {
      case Success(stream) =>
        Logger.info("Document source stream created.")
        val s: Source[User, Future[State]] = stream.documentSource()
        s.runForeach(f)
      case _ =>
        Logger.info("Failed to create cursor.")
    }
  }
}

class UserRepository @Inject()(implicit val ec: ExecutionContext) extends UserRepositoryT

object UserRepository extends App with UserRepositoryT {
  override val ec = global
  listenUserCollection { user =>
    Logger.info(s"==== Created new user: $user")
  }
  create(User("xxx", "jo", "j@j.org"))
}
