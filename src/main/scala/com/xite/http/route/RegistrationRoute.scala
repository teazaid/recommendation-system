package com.xite.http.route

import _root_.com.xite.validation.{RegisterUserRequestRequestValidator, RequestValidator}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.xite.http.model.{RegisterUserRequest, RegisterUserResponse}
import com.xite.service.UserService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class RegistrationRoute(userService: UserService)
                       (implicit val executionContext: ExecutionContext) extends BaseRoute[RegisterUserRequest] {

  override protected def validator: RequestValidator[RegisterUserRequest] = RegisterUserRequestRequestValidator

  def route: Route = path("register") {
    handleRejections(registrationRejectionHandler) {
      post {
        entity[RegisterUserRequest](as[RegisterUserRequest]) { request =>
          validateModel(request) { validatedRequest =>
            val registerUserInfo = userService.register(validatedRequest.userName,
              validatedRequest.email,
              validatedRequest.age,
              validatedRequest.gender
            )

            onComplete(registerUserInfo) {
              case Success(registerUserInfo) => complete(RegisterUserResponse(registerUserInfo.userId, registerUserInfo.videoId))
              case Failure(t) => complete(StatusCodes.InternalServerError -> s"failed to register a user with error: ${t.getMessage}")
            }
          }
        }
      }
    }
  }
}
