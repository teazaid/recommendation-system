package com.xite.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.xite.model.{UserId, VideoId}

class UserActor(userId: UserId) extends Actor with ActorLogging {

  override def receive: Receive = {
    case WatchNewVideo(videoId) =>
      log.debug("User: {} is watching video: {}", userId, videoId)
      context.become(watchingVideoContext(videoId))
    case unexpectedMsg => log.error("User: {} received unexpected message {}", userId, unexpectedMsg)
  }

  private def watchingVideoContext(videoId: VideoId): Receive = {
    case GetLastViewedVideo =>
      log.debug("User: {} watched video: {}", userId, videoId)
      sender() ! LastViewedVideo(videoId)

    case WatchNewVideo(newVideoId) =>
      log.debug("User: {} is watching video: {}", userId, videoId)
      context.become(watchingVideoContext(newVideoId))

    case unexpectedMsg => log.error("User: {} received unexpected message {}", userId, unexpectedMsg)
  }
}

object UserActor {
  def props(userId: UserId): Props = Props(classOf[UserActor], userId)
}

final case class WatchNewVideo(videoId: VideoId)

final case class LastViewedVideo(videoId: VideoId)
