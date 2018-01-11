package com.xite.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserActorSpec extends TestKit(ActorSystem("system")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private val videoId = 123
  private val newVideoId = 456
  private val userId = 123l

  "UserActor" should {
    "be able to watch video and return last watched video" in {
      val userActor = system.actorOf(UserActor.props(userId))
      userActor ! WatchNewVideo(videoId)
      userActor ! GetLastViewedVideo
      expectMsg(LastViewedVideo(videoId))

      userActor ! WatchNewVideo(newVideoId)
      userActor ! GetLastViewedVideo
      expectMsg(LastViewedVideo(newVideoId))
    }
  }
}
