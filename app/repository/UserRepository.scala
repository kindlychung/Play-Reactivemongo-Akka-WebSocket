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
  val reactiveMongoApi: ReactiveMongoApi

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  implicit def userWriter = Macros.writer[User]

  implicit def userReader = Macros.reader[User]


  lazy val collection: Future[BSONCollection] = reactiveMongoApi.database.map{db =>
    db.collection("users")
  }

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

  def listenUserCollection(f: User => Unit): Unit = {
    val userStreamSource: Future[Source[User, Future[State]]] = createCursor.map { cursor =>
      cursor.documentSource()
    }
    userStreamSource.onComplete {
      case Success(stream) =>
        Logger.info("Document source stream created.")
        stream.runForeach(f)
      case _ =>
        Logger.info("Failed to create cursor.")
    }
  }
}

class UserRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi, implicit val ec: ExecutionContext) extends UserRepositoryT

