package models

import play.api.libs.json._

case class User(firstName: String, lastName: String, email: String)

object User {
  implicit val jsonFormat = Json.format[User]
}

