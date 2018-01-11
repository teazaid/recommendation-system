package com.xite.validation

import cats.data.{NonEmptyList, Validated}
import com.xite.http.model.ActionRequest

object ActionRequestValidator extends RequestValidator[ActionRequest]{
  private val InvalidActionIdErrorMessage = "actionId is not valid"
  private val InvalidActionId = NonEmptyList.of(InvalidActionIdErrorMessage)

  private val allowedActions = Set(1, 2, 3)

  override def validate(actionRequest: ActionRequest): Validated[NonEmptyList[String], ActionRequest] =
    Validated.cond(allowedActions.contains(actionRequest.actionId), actionRequest, InvalidActionId)
}
