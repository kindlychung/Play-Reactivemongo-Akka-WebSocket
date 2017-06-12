package controllers

import javax.inject.Inject

import actors.UserListenActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc.{Action, Controller, WebSocket}
import models._
import models.User._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject()(userRepository: UserRepository,
                               implicit val system: ActorSystem,
                               implicit val mat: Materializer) extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def create = Action.async(parse.json) {
    _.body.validate[User].map(user =>
      userRepository.create(user).map(_ => Created)
    ).getOrElse(Future.successful(BadRequest("invalid json")))
  }

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[User, User]

  def watchCollection = WebSocket.accept[User, User] { _ =>
    ActorFlow.actorRef(out => UserListenActor.props(userRepository, out))
  }

}