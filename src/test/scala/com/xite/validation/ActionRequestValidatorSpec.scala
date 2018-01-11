package com.xite.validation

import org.scalatest.{Matchers, WordSpec}
import cats.syntax.validated._
import com.xite.http.model.ActionRequest

class ActionRequestValidatorSpec extends WordSpec with Matchers {
  private val InvalidActionIdErrorMessage = "actionId is not valid"

  "ActionRequestValidator" should {
    "validate actionId" in {
      ActionRequestValidator.validate(ActionRequest(1, 2, 1)) shouldEqual ActionRequest(1, 2, 1).valid
      ActionRequestValidator.validate(ActionRequest(1, 2, 2)) shouldEqual ActionRequest(1, 2, 2).valid
      ActionRequestValidator.validate(ActionRequest(1, 2, 3)) shouldEqual ActionRequest(1, 2, 3).valid

      ActionRequestValidator.validate(ActionRequest(1, 2, 0)) shouldEqual InvalidActionIdErrorMessage.invalidNel
      ActionRequestValidator.validate(ActionRequest(1, 2, 4)) shouldEqual InvalidActionIdErrorMessage.invalidNel
    }
  }
}
