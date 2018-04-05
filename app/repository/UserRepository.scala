package repository

import javax.inject.Inject
import reactivemongo.api._
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.scaladsl.Source
import models._
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, Macros}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait UserRepositoryT {

  import reactivemongo.akkastream._

  implicit val ec: ExecutionContext

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  implicit def userWriter = Macros.writer[User]

  implicit def userReader = Macros.reader[User]


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

  def create(user: User, out: Option[ActorRef] = None): Future[WriteResult] = {
    collection.flatMap(_.insert(user)).map{result =>
      out.foreach {
        _ ! user
      }
      result
    }
  }

  private def createCursor: Future[AkkaStreamCursor[User]] = {
    collection.map { coll =>
      coll.find(BSONDocument())
        .tailable
        .cursor[User]()
    }
  }

  val c = createCursor
  var userStreamSource: Future[Source[User, Future[State]]] = c.map { cursor =>
    cursor.documentSource()
  }
  def listenUserCollection(f: User => Unit): Unit = {
    c.onComplete {
      case Success(stream) =>
        Logger.info("Document source stream created.")
        userStreamSource.foreach(x => x.runForeach(f))
      case _ =>
        Logger.info("Failed to create cursor.")
    }
  }
}

class UserRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi, implicit val ec: ExecutionContext) extends UserRepositoryT

object UserRepository extends App with UserRepositoryT {
  val ec = scala.concurrent.ExecutionContext.Implicits.global
  listenUserCollection { user =>
    Logger.info(s"==== Created new user: $user")
  }
  create(User("1246", "jo", "j@j.org"))
}
