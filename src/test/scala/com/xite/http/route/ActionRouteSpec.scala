package com.xite.http.route

import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import cats.data.{NonEmptyList, Validated}
import com.xite.model._
import com.xite.service.UserService
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import cats.syntax.validated._
import com.xite.http.model.{ActionRequest, ErrorResponse}
import cats.syntax.either._
import scala.concurrent.Future

class ActionRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with PredefinedToResponseMarshallers with MockFactory {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  "ActionRoute" should {
    "reject request with invalid actionId" in new ActionRouteFixture {
      val invalidActionId = 0
      val request = ByteString(ActionRequest(userId, videoId, invalidActionId).asJson.noSpaces)
      Post("/action",
        HttpEntity(MediaTypes.`application/json`, request)) ~> route ~> check {
        status.intValue() shouldEqual 400
        responseAs[ErrorResponse].asJson shouldEqual
          parse(""" { "errors": [ "actionId is not valid"] }""").right.get
      }
    }

    "return internal server error if failed to perform Action" in new ActionRouteFixture {
      val errorMessage = "Action Error"

      (userServiceMock.action(_: UserId, _: VideoId, _: ActionId))
        .expects(userId, videoId, actionId)
        .returning(Future.failed(new Exception(errorMessage)))

      val request = ByteString(ActionRequest(userId, videoId, actionId).asJson.noSpaces)
      Post("/action",
        HttpEntity(MediaTypes.`application/json`, request)) ~> route ~> check {
        status.intValue() shouldEqual 500
        responseAs[String] shouldEqual s"failed to perform Action with error: ${errorMessage}"
      }
    }

    "return error message if failed validation during performing action" in new ActionRouteFixture {
      val validationErrorMessage = "User doesn't exist"
      (userServiceMock.action(_: UserId, _: VideoId, _: ActionId))
        .expects(userId, videoId, actionId)
        .returning(Future.successful(NonEmptyList.of(validationErrorMessage).asLeft[ActionPerformedInfo]))

      val request = ByteString(ActionRequest(userId, videoId, actionId).asJson.noSpaces)
      Post("/action",
        HttpEntity(MediaTypes.`application/json`, request)) ~> route ~> check {
        status.intValue() shouldEqual 400
        responseAs[ErrorResponse].asJson shouldEqual
          parse(""" { "errors": [ "User doesn't exist"] }""").right.get
      }
    }

    "performing action successfully" in new ActionRouteFixture {
      (userServiceMock.action(_: UserId, _: VideoId, _: ActionId))
        .expects(userId, videoId, actionId)
        .returning(Future.successful(ActionPerformedInfo(userId, videoId).asRight[NonEmptyList[String]]))

      val request = ByteString(ActionRequest(userId, videoId, actionId).asJson.noSpaces)
      Post("/action",
        HttpEntity(MediaTypes.`application/json`, request)) ~> route ~> check {
        status.intValue() shouldEqual 200
        responseAs[ActionPerformedInfo].asJson shouldEqual parse(s"""{ "userId": ${userId}, "videoId":${videoId} }""").right.get
      }
    }
  }

  trait ActionRouteFixture {
    val videoId = 777
    val userId = 555
    val actionId = 1

    val userServiceMock = mock[UserService]
    val route = new ActionRoute(userServiceMock).route
  }

}
