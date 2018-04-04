package repository

import javax.inject.Inject
import reactivemongo.api._
import reactivemongo.akkastream.cursorProducer
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.scaladsl.Source
import com.google.inject.Guice
import models._
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.akkastream.{AkkaStreamCursor, State}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.util.Success
import play.modules.reactivemongo.ReactiveMongoModule

trait UserRepositoryT {
  def reactiveMongoApi: ReactiveMongoApi

  implicit def ec: ExecutionContext

  val collection = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  def create(user: User) = {
    collection.map(_.insert(user))
  }

  private def createCursor: Future[AkkaStreamCursor[User]] = {
    collection.map(_.find(BSONDocument())
      .options(QueryOpts().tailable.awaitData)
      .cursor[User]())
  }

  def listenUserCollection(f: User => Unit): Unit = {
    Logger.info("Inside listenUserCollection...")
    val c = createCursor
    c.onComplete {
      case Success(stream) =>
        Logger.info("New element received...")
        val s: Source[User, Future[State]] = stream.documentSource()
        s.runForeach(f)
      case _ =>
        Logger.info("Failed to create cursor.")
    }
  }
}

class UserRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi, implicit val ec: ExecutionContext) extends UserRepositoryT

object UserRepository extends App with UserRepositoryT {
  private val injector =    play.api.Play.current.injector
  override val reactiveMongoApi = injector.instanceOf[ReactiveMongoApi]
  override val ec = global
  listenUserCollection { user =>
    println(user)
  }
}
