package com.xite.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import cats.data.NonEmptyList
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import cats.syntax.validated._
import cats.syntax.either._
import scala.concurrent.duration._

class UserSupervisorActorSpec extends TestKit(ActorSystem("system")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private val movieId = 1
  private val userId1 = 1
  private val userId2 = 2
  private val leastViewedVideoId1 = 666
  private val leastViewedVideoId2 = 222
  private val actionId = 1

  private val registerUser = RegisterUser("userName", "email", 28, 1)

  "UserSupervisorActor" should {
    val videoManagerActor = TestProbe()

    val userSupervisorActor = system.actorOf(UserSupervisorActor.props(videoManagerActor.ref, Timeout(1.second)), "userSupervisor")

    "return error if userId does't exist" in {
      userSupervisorActor ! PerformVideoAction(userId1, movieId, actionId)
      expectMsg(VideoActionResult(NonEmptyList.of("userId does not exist").asLeft[WatchingNewVideo]))
    }

    "spawn UserActors with sequential userId's" in {
      userSupervisorActor ! registerUser
      videoManagerActor.expectMsg(WatchLeastViewedVideo)
      videoManagerActor.reply(LeastViewedVideo(leastViewedVideoId1))
      expectMsg(RegisteredUser(userId1, leastViewedVideoId1))

      userSupervisorActor ! registerUser
      videoManagerActor.expectMsg(WatchLeastViewedVideo)
      videoManagerActor.reply(LeastViewedVideo(leastViewedVideoId2))
      expectMsg(RegisteredUser(userId2, leastViewedVideoId2))
    }

    "return error if videoId doesn't correspond to last given" in {
      userSupervisorActor ! PerformVideoAction(userId1, movieId + 1, actionId)
      expectMsg(VideoActionResult(NonEmptyList.of("video does not correspond to last given").asLeft[WatchingNewVideo]))
    }

    "rate movie for a user" in {
      userSupervisorActor ! PerformVideoAction(userId1, leastViewedVideoId1, actionId)
      videoManagerActor.expectMsg(WatchLeastViewedVideo)
      videoManagerActor.reply(LeastViewedVideo(leastViewedVideoId1))
      expectMsg(VideoActionResult(WatchingNewVideo(userId1, leastViewedVideoId1).asRight[NonEmptyList[String]]))
    }
  }
}
