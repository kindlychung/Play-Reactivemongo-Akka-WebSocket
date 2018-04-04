package modules

import com.google.inject.AbstractModule
import play.modules.reactivemongo.{DefaultReactiveMongoApi, ReactiveMongoApi}
import repository.{UserRepository, UserRepositoryT}

class AppInjectory extends AbstractModule{
  override def configure(): Unit = {
    bind(classOf[UserRepositoryT]).to(classOf[UserRepository])
    bind(classOf[ReactiveMongoApi]).to(classOf[DefaultReactiveMongoApi])
  }
}
