package com.xite.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.xite.model.VideoId

class VideoManagerActor(listOfVideos: List[VideoId]) extends Actor with ActorLogging {

  log.debug("list of available videos: {}", listOfVideos.mkString(", "))

  override def receive: Receive = watchingContext(listOfVideos.map((_ -> 0L)).toMap)

  private def watchingContext(videosRating: Map[VideoId, Long]): Receive = {
    case WatchLeastViewedVideo =>
      val (leastViewedVideo, amountOfViews) = videosRating.minBy { case (_, amountOfViews) => amountOfViews }
      log.debug(s"Least viewed video {} out of {}", leastViewedVideo, videosRating.mkString(", "))

      sender() ! LeastViewedVideo(leastViewedVideo)
      val updateVideosRating = videosRating + (leastViewedVideo -> (amountOfViews + 1L))
      context.become(watchingContext(updateVideosRating))
  }
}

object VideoManagerActor {
  def props(listOfVideos: List[VideoId]): Props = Props(classOf[VideoManagerActor], listOfVideos)
}

final case class LeastViewedVideo(videoId: VideoId)

final case object WatchLeastViewedVideo
