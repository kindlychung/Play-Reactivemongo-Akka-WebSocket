package actors

import actors.UserListenActor.ReceiveUpdate
import akka.actor.{Actor, ActorRef, Props}
import models.User
import play.api.Logger
import repository.UserRepository

object UserListenActor {

  case class ReceiveUpdate(user: User)

  def props(userRepository: UserRepository, out: ActorRef): Props = Props(new UserListenActor(userRepository, out))
}

class UserListenActor(userRepository: UserRepository, out: ActorRef) extends Actor with akka.actor.ActorLogging {

  override def receive: PartialFunction[Any, Unit] = {
    case ReceiveUpdate(u) => {
      Logger.info("Update received!")
      out ! u
    }
  }

  override def preStart: Unit = {
    val func: User => Unit = (u: User) => {
      Logger.info("About to update!")
      self ! ReceiveUpdate(u)
    }
    val user = User("first", "second", "a@b.org")
    self ! ReceiveUpdate(user)
    Logger.info("Add listener hook...")
    userRepository.listenUserCollection(func)
  }

}
