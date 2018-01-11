package com.xite.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import cats.data.{NonEmptyList}
import com.xite.model._
import cats.syntax.either._

class UserSupervisorActor(videoManagerActor: ActorRef, implicit val timeout: Timeout) extends Actor with ActorLogging {

  private implicit val dispatcher = context.dispatcher

  override def receive: Receive = registerUserContext(1)

  private def registerUserContext(nextId: Long): Receive = {
    case RegisterUser(userName, email, age, gender) =>
      log.debug("Registering new User with userName:{}, email: {}, age: {}, gender: {}", userName, email, age, gender)
      val originalSender = sender()
      val newUserActor = context.actorOf(UserActor.props(nextId), getUserActorName(nextId))

      for {
        leastViewedVideo <- (videoManagerActor ? WatchLeastViewedVideo).mapTo[LeastViewedVideo]
      } yield {
        newUserActor ! WatchNewVideo(leastViewedVideo.videoId)
        originalSender ! RegisteredUser(nextId, leastViewedVideo.videoId)
      }

      context.become(registerUserContext(nextId + 1))

    case PerformVideoAction(userId, videoId, actionId) =>
      log.debug("Received PerformVideoAction userId:{}, videoId: {}, actionId: {}", userId, videoId, actionId)
      val originalSender = sender()

      context.child(getUserActorName(userId)).map { userActor =>
        performActionUnderLastViewedVideo(userActor, originalSender, userId, videoId)
      }.getOrElse {
        log.debug("User with userId:{} doesn't exist", userId)
        originalSender ! VideoActionResult(NonEmptyList.of("userId does not exist").asLeft[WatchingNewVideo])
      }
  }

  private def performActionUnderLastViewedVideo(userActor: ActorRef, originalSender: ActorRef, userId: UserId, videoId: VideoId): Unit = {
    (userActor ? GetLastViewedVideo).mapTo[LastViewedVideo].foreach { lastWatchedVideo =>
      if (videoId != lastWatchedVideo.videoId) {
        log.debug("Video {} doesn't correspond to last watched {} for  userId:{}", videoId, lastWatchedVideo.videoId, userId)
        originalSender ! VideoActionResult(NonEmptyList.of("video does not correspond to last given").asLeft[WatchingNewVideo])
      }
      else {
        val watchingNewVideoF = (videoManagerActor ? WatchLeastViewedVideo).mapTo[LeastViewedVideo].map { leastViewedVideo =>
          userActor ! WatchNewVideo(leastViewedVideo.videoId)
          VideoActionResult(WatchingNewVideo(userId, leastViewedVideo.videoId).asRight[NonEmptyList[String]])
        }

        watchingNewVideoF.pipeTo(originalSender)
      }
    }
  }

  private def getUserActorName(userId: UserId): String = {
    s"userActor-${userId}"
  }
}

object UserSupervisorActor {
  def props(videoManagerActor: ActorRef, timeout: Timeout): Props = Props(classOf[UserSupervisorActor], videoManagerActor, timeout)
}

final case class RegisterUser(userName: UserName, email: Email, age: Age, gender: Gender)

final case class RegisteredUser(userId: UserId, videoId: VideoId)

final case class PerformVideoAction(userId: UserId, videoId: VideoId, actionId: ActionId)

final case class WatchingNewVideo(userId: UserId, videoId: VideoId)

final case class VideoActionResult(result: Either[NonEmptyList[String], WatchingNewVideo])

final case object GetLastViewedVideo