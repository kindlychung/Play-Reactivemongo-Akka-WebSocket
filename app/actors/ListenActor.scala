package actors

import actors.UserListenActor.ReceiveUpdate
import akka.actor.{Actor, ActorRef, Props}
import models.User
import repository.UserRepository

object UserListenActor {

  case class ReceiveUpdate(user: User);

  def props(userRepository: UserRepository, out: ActorRef): Props = Props(new UserListenActor(userRepository, out))
}

class UserListenActor(userRepository: UserRepository, out: ActorRef) extends Actor {

  override def receive = {
    case ReceiveUpdate(u) => out ! u
  }

  override def preStart = {
    val func = (u: User) => self ! ReceiveUpdate(u);
    Some(userRepository.listenUserCollection(func))
  }

}
