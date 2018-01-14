package com.xite.service

import cats.data.NonEmptyList
import com.xite.model._

import scala.concurrent.Future

trait UserService {
  def register(userName: UserName, email: Email, age: Age, gender: Gender): Future[RegisteredUserInfo]
  def action(userId: UserId, videoId: VideoId, actionId: ActionId): Future[Either[NonEmptyList[String], ActionPerformedInfo]]
}
