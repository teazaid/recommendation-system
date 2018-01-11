package com.xite.http

import akka.http.scaladsl.server.Rejection
import cats.data.NonEmptyList

final case class XiteValidationRejection(errors: NonEmptyList[String]) extends Rejection
