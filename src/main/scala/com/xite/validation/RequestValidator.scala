package com.xite.validation

import cats.data.{NonEmptyList, Validated}

trait RequestValidator[T] {
  def validate(request: T): Validated[NonEmptyList[String], T]
}
