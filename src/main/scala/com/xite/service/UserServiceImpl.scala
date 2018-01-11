package com.xite.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import cats.data.NonEmptyList
import com.xite.actor._
import com.xite.model._

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(userSupervisorActor: ActorRef)
                     (implicit val executionContext: ExecutionContext, val actorTimeout: Timeout) extends UserService {

  override def register(userName: UserName,
                        email: Email,
                        age: Age,
                        gender: Gender): Future[RegisteredUserInfo] = {
    (userSupervisorActor ? RegisterUser(userName, email, age, gender)).mapTo[RegisteredUser].map { registeredUser =>
      RegisteredUserInfo(registeredUser.userId, registeredUser.videoId)
    }
  }

  override def action(userId: UserId,
                      videoId: VideoId,
                      actionId: ActionId): Future[Either[NonEmptyList[String], ActionPerformedInfo]] = {
    (userSupervisorActor ? PerformVideoAction(userId, videoId, actionId))
      .mapTo[VideoActionResult].map { videoActionResult =>
      videoActionResult.result.map(watchingNewVideo =>
        ActionPerformedInfo(watchingNewVideo.userId, watchingNewVideo.videoId))
    }
  }
}
