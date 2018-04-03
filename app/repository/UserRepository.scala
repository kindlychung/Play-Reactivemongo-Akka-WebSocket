package repository

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models._
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.akkastream.AkkaStreamCursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class UserRepository @Inject()(reactiveMongoApi: ReactiveMongoApi, implicit val ec: ExecutionContext) {

  val collection = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))

  def create(user: User) = {
    collection.map(_.insert(user))
  }

  private def createCursor: Future[AkkaStreamCursor[User]] = {
    import reactivemongo.api._
    import reactivemongo.akkastream.cursorProducer

    collection.map(_.find(BSONDocument())
      .options(QueryOpts().tailable.awaitData)
      .cursor[User]())
  }

  def listenUserCollection(f: User => Unit) {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val c = createCursor
    c.onComplete {
      case Success(stream) =>
        Logger.info("New element received...")
        stream.documentSource().runForeach(f)
      case _ =>
        Logger.info("Failed to create cursor.")
    }
  }

}
