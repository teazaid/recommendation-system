package com.xite.service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActor, TestKit, TestProbe}
import akka.util.Timeout
import cats.data.NonEmptyList
import com.xite.actor._
import com.xite.model._
import org.scalatest.{Matchers, WordSpecLike}
import cats.syntax.either._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.syntax.validated._

class UserServiceSpec extends TestKit(ActorSystem("system")) with ImplicitSender with WordSpecLike with Matchers {
  private val userName: UserName = "userName"
  private val email: Email = "email"
  private val age: Age = 1
  private val gender: Gender = 2
  private val userId: UserId = 777
  private val videoId: VideoId = 555
  private val actionId: ActionId = 3

  private val timeout = 1.second
  private implicit val actorTimeout = Timeout(timeout)

  "UserService" should {
    "register new user" in {
      val userSupervisorActor = TestProbe()

      userSupervisorActor.setAutoPilot((sender: ActorRef, _: Any) => {
        sender ! RegisteredUser(userId, videoId)
        TestActor.KeepRunning
      })

      val userService = new UserServiceImpl(userSupervisorActor.ref)
      val result = Await.result(userService.register(userName, email, age, gender), timeout)

      userSupervisorActor.expectMsg(RegisterUser(userName, email, age, gender))
      result shouldEqual RegisteredUserInfo(userId, videoId)
    }

    "return validation error if user doesn't exist" in {
      val userDoesntExistError = NonEmptyList.of("userId does not exist").asLeft[WatchingNewVideo]
      val userSupervisorActor = TestProbe()

      userSupervisorActor.setAutoPilot((sender: ActorRef, _: Any) => {
        sender ! VideoActionResult(userDoesntExistError)
        TestActor.KeepRunning
      })

      val userService = new UserServiceImpl(userSupervisorActor.ref)
      val result = Await.result(userService.action(userId, videoId, actionId), timeout)

      userSupervisorActor.expectMsg(PerformVideoAction(userId, videoId, actionId))
      result shouldEqual userDoesntExistError
    }

    "return error message if video doesn't correspond to last one" in {
      val VideoDoesNotCorrespondToLastGivenError = NonEmptyList.of("video does not correspond to last given").asLeft[WatchingNewVideo]
      val userSupervisorActor = TestProbe()

      userSupervisorActor.setAutoPilot((sender: ActorRef, _: Any) => {
        sender ! VideoActionResult(VideoDoesNotCorrespondToLastGivenError)
        TestActor.KeepRunning
      })

      val userService = new UserServiceImpl(userSupervisorActor.ref)
      val result = Await.result(userService.action(userId, videoId, actionId), timeout)

      userSupervisorActor.expectMsg(PerformVideoAction(userId, videoId, actionId))
      result shouldEqual VideoDoesNotCorrespondToLastGivenError
    }

    "perform action successfully" in {
      val userSupervisorActor = TestProbe()

      userSupervisorActor.setAutoPilot((sender: ActorRef, _: Any) => {
        sender ! VideoActionResult(WatchingNewVideo(userId, videoId).asRight[NonEmptyList[String]])
        TestActor.KeepRunning
      })

      val userService = new UserServiceImpl(userSupervisorActor.ref)
      val result = Await.result(userService.action(userId, videoId, actionId), timeout)

      userSupervisorActor.expectMsg(PerformVideoAction(userId, videoId, actionId))
      result shouldEqual ActionPerformedInfo(userId, videoId).asRight[NonEmptyList[String]]
    }
  }
}
