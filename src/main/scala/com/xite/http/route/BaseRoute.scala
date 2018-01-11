package com.xite.http.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, provide, reject}
import akka.http.scaladsl.server.{Directive1, RejectionHandler, Route}
import cats.data.Validated.{Invalid, Valid}
import com.xite.http.XiteValidationRejection
import com.xite.http.model.ErrorResponse
import com.xite.validation.RequestValidator
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

trait BaseRoute[T] {

  implicit val executionContext: ExecutionContext

  protected def validator: RequestValidator[T]

  protected val registrationRejectionHandler =
    RejectionHandler.newBuilder().handle {
      case XiteValidationRejection(error) => complete((StatusCodes.BadRequest, ErrorResponse(error.toList)))
    }.result()

  protected def validateModel(body: T): Directive1[T] = {
    validator.validate(body) match {
      case Valid(validatedRegisterUserRequest) => provide[T](validatedRegisterUserRequest)
      case Invalid(reasons) => reject(XiteValidationRejection(reasons))
    }
  }

  def route: Route
}
