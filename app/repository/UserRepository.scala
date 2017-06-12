package repository

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.akkastream.AkkaStreamCursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

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
    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()
    createCursor.map(_.documentSource().runForeach(f)(mat))
  }

}
