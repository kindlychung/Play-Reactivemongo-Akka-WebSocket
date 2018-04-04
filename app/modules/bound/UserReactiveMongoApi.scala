package modules.bound

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.play.json.JSONSerializationPack

import scala.concurrent.Future

class UserReactiveMongoApi extends ReactiveMongoApi {
  override def driver: MongoDriver = ???

  override def connection: MongoConnection = ???

  override def database: Future[DefaultDB] = ???

  override def asyncGridFS: Future[GridFS[JSONSerializationPack.type]] = ???

  override def db: DefaultDB = ???

  override def gridFS: GridFS[JSONSerializationPack.type] = ???
}
