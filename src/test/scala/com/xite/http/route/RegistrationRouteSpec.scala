package com.xite.http.route

import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.xite.http.model.{ErrorResponse, RegisterUserRequest, RegisterUserResponse}
import com.xite.model._
import com.xite.service.UserService
import org.scalatest.{Matchers, WordSpec}
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import scala.concurrent.Future

class RegistrationRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with PredefinedToResponseMarshallers with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  "RegistrationRoute" should {
    "register user" in new RegistrationRouteFixture {
      val userId = 12345
      val videoId = 432455

      val registerRequest = ByteString(RegisterUserRequest(userName, email, age, gender).asJson.noSpaces)
      (userServiceMock.register(_: UserName, _: Email, _: Age, _: Gender))
        .expects(userName, email, age, gender)
        .returns(Future.successful(RegisteredUserInfo(userId, videoId)))

      Post("/register",
        HttpEntity(MediaTypes.`application/json`, registerRequest)) ~> route ~> check {
        status.intValue() shouldEqual 200
        responseAs[RegisterUserResponse].asJson shouldEqual parse(s"""{ "userId": ${userId}, "videoId":${videoId} }""").right.get
      }
    }

    "return internal server error if failed to register a user" in new RegistrationRouteFixture {
      val errorMessage = "Registration Error"
      val registerRequest = ByteString(RegisterUserRequest(userName, email, age, gender).asJson.noSpaces)
      (userServiceMock.register(_: UserName, _: Email, _: Age, _: Gender))
        .expects(userName, email, age, gender)
        .returns(Future.failed(new Exception(errorMessage)))

      Post("/register",
        HttpEntity(MediaTypes.`application/json`, registerRequest)) ~> route ~> check {
        status.intValue() shouldEqual 500
        responseAs[String] shouldEqual s"failed to register a user with error: ${errorMessage}"
      }
    }

    "return validation errors in the response" in new RegistrationRouteFixture {
      val invalidRequest = ByteString(RegisterUserRequest(userName, "email", 4, 3).asJson.noSpaces)
      Post("/register",
        HttpEntity(MediaTypes.`application/json`, invalidRequest)) ~> route ~> check {
        status.intValue() shouldEqual 400
        responseAs[ErrorResponse].asJson shouldEqual
          parse(""" { "errors": [ "email is not valid", "age is not valid", "gender is not valid"] }""").right.get
      }
    }
  }

  trait RegistrationRouteFixture {
    val email = "email@gmail.com"
    val age = 5
    val gender = 1
    val userName = "userName"

    val userServiceMock = mock[UserService]
    val route = new RegistrationRoute(userServiceMock).route
  }

}
