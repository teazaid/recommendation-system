package com.xite.validation

import cats.data.{NonEmptyList, Validated}
import org.scalatest.{Matchers, WordSpec}
import cats.syntax.validated._
import com.xite.http.model.RegisterUserRequest

class RegisterUserRequestRequestValidatorSpec extends WordSpec with Matchers {
  import RegisterUserRequestRequestValidator._
  private val male = 1
  private val female = 2

  private val InvalidEmailErrorMessage = "email is not valid"
  private val InvalidAgeErrorMessage = "age is not valid"
  private val InvalidGenderErrorMessage = "gender is not valid"

  private val validEmail = "test@gmail.com"
  private val userName = "userName"

  "RegisterUserRequestValidator" should {
    "validate age" in {
      val validRegisterUserRequest1 = RegisterUserRequest(userName, validEmail, 5, male)
      val validRegisterUserRequest2 = RegisterUserRequest(userName, validEmail, 120, male)

      val invalidRegisterUserRequestAge1 = RegisterUserRequest(userName, validEmail, 4, male)
      val invalidRegisterUserRequestAge2 = RegisterUserRequest(userName, validEmail, 0, male)
      val invalidRegisterUserRequestAge3 = RegisterUserRequest(userName, validEmail, 121, male)

      validate(validRegisterUserRequest1) shouldEqual validRegisterUserRequest1.valid
      validate(validRegisterUserRequest2) shouldEqual validRegisterUserRequest2.valid

      validate(invalidRegisterUserRequestAge1) shouldEqual InvalidAgeErrorMessage.invalidNel
      validate(invalidRegisterUserRequestAge2) shouldEqual InvalidAgeErrorMessage.invalidNel
      validate(invalidRegisterUserRequestAge3) shouldEqual InvalidAgeErrorMessage.invalidNel
    }

    "validate gender" in {
      val validRegisterUserRequest1 = RegisterUserRequest(userName, validEmail, 5, male)
      val validRegisterUserRequest2 = RegisterUserRequest(userName, validEmail, 5, female)

      val invalidRegisterUserRequestGender1 = RegisterUserRequest(userName, validEmail, 5, 0)
      val invalidRegisterUserRequestGender2 = RegisterUserRequest(userName, validEmail, 5, 3)

      validate(validRegisterUserRequest1) shouldEqual validRegisterUserRequest1.valid
      validate(validRegisterUserRequest2) shouldEqual validRegisterUserRequest2.valid

      validate(invalidRegisterUserRequestGender1) shouldEqual InvalidGenderErrorMessage.invalidNel
      validate(invalidRegisterUserRequestGender2) shouldEqual InvalidGenderErrorMessage.invalidNel
    }

    "validate email" in {
      val validRegisterUserRequest = RegisterUserRequest(userName, validEmail, 5, male)

      validate(validRegisterUserRequest) shouldEqual validRegisterUserRequest.valid

      validate(RegisterUserRequest(userName, null, 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel
      validate(RegisterUserRequest(userName, "testgmail.com", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel
      validate(RegisterUserRequest(userName, "test-gmail.com", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel

      validate(RegisterUserRequest(userName, "test@gma.i.l.c.o.m", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel

      validate(RegisterUserRequest(userName, "test@gmailcom", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel
      validate(RegisterUserRequest(userName, "t es t@gmailcom", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel
      validate(RegisterUserRequest(userName, "b ob @tes tmai l.com", 5, male)) shouldEqual InvalidEmailErrorMessage.invalidNel
    }

    "invalid both" in {
      validate(RegisterUserRequest(userName, "testgmail.com", 4, 3)) shouldEqual Validated.invalid[NonEmptyList[String], RegisterUserRequest](NonEmptyList.of(InvalidEmailErrorMessage, InvalidAgeErrorMessage, InvalidGenderErrorMessage))
      validate(RegisterUserRequest(userName, validEmail, 4, 3)) shouldEqual Validated.invalid[NonEmptyList[String], RegisterUserRequest](NonEmptyList.of(InvalidAgeErrorMessage, InvalidGenderErrorMessage))

      validate(RegisterUserRequest(userName, "testgmail.com", 4, 1)) shouldEqual Validated.invalid[NonEmptyList[String], RegisterUserRequest](NonEmptyList.of(InvalidEmailErrorMessage, InvalidAgeErrorMessage))
      validate(RegisterUserRequest(userName, "testgmail.com", 5, 0)) shouldEqual Validated.invalid[NonEmptyList[String], RegisterUserRequest](NonEmptyList.of(InvalidEmailErrorMessage, InvalidGenderErrorMessage))
    }
  }
}
