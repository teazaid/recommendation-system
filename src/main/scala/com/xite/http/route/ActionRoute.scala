package com.xite.http.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, handleRejections, onComplete, path, post}
import akka.http.scaladsl.server.Route
import com.xite.http.model.{ActionRequest, ActionResponse, ErrorResponse}
import com.xite.service.UserService
import com.xite.validation.{ActionRequestValidator, RequestValidator}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class ActionRoute(userService: UserService)(implicit val executionContext: ExecutionContext) extends BaseRoute[ActionRequest] {

  override protected implicit def validator: RequestValidator[ActionRequest] = ActionRequestValidator

  def route: Route = path("action") {
    handleRejections(registrationRejectionHandler) {
      post {
        entity[ActionRequest](as[ActionRequest]) { request =>
          validateModel(request) { validatedRequest =>
            val actionResult = userService.action(validatedRequest.userId, validatedRequest.videoId, validatedRequest.actionId)

            onComplete(actionResult) {
              case Success(Right(actionPerformedInfo)) =>
                complete(StatusCodes.OK -> ActionResponse(actionPerformedInfo.userId, actionPerformedInfo.videoId))
              case Success(Left(validationErrors)) =>
                complete(StatusCodes.BadRequest -> ErrorResponse(validationErrors.toList))
              case Failure(t) =>
                complete(StatusCodes.InternalServerError -> s"failed to perform Action with error: ${t.getMessage}")
            }
          }
        }
      }
    }
  }
}
