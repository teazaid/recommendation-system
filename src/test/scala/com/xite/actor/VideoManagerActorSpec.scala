package com.xite.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.xite.model.VideoId
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class VideoManagerActorSpec extends TestKit(ActorSystem("system")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private val videoId1 = 1
  private val videoId2 = 2
  private val videoId3 = 3

  private val listOfVideos = List[VideoId](videoId1, videoId2, videoId3)

  "VideoManagerActor" should {
    val videoManagerActor = system.actorOf(VideoManagerActor.props(listOfVideos))

    "return least viewed video" in {
      videoManagerActor ! WatchLeastViewedVideo
      expectMsg(LeastViewedVideo(videoId1))

      videoManagerActor ! WatchLeastViewedVideo
      expectMsg(LeastViewedVideo(videoId2))

      videoManagerActor ! WatchLeastViewedVideo
      expectMsg(LeastViewedVideo(videoId3))

      videoManagerActor ! WatchLeastViewedVideo
      expectMsg(LeastViewedVideo(videoId1))
    }
  }
}
