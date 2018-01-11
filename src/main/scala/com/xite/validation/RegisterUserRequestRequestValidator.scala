package com.xite.validation

import cats.Applicative
import cats.data.{NonEmptyList, Validated}
import com.xite.http.model.RegisterUserRequest

object RegisterUserRequestRequestValidator extends RequestValidator[RegisterUserRequest] {
  private val InvalidEmailErrorMessage = "email is not valid"
  private val InvalidAgeErrorMessage = "age is not valid"
  private val InvalidGenderErrorMessage = "gender is not valid"

  private val InvalidEmail = NonEmptyList.of(InvalidEmailErrorMessage)
  private val InvalidAge = NonEmptyList.of(InvalidAgeErrorMessage)
  private val InvalidGender = NonEmptyList.of(InvalidGenderErrorMessage)

  private val emailRegex = """^[-a-z0-9!#$%&'*+/=?^_`{|}~]+(\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@([a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\.)*(aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$""".r

  private val minAge = 5
  private val maxAge = 120

  private val allowedGenders = Set(1, 2)

  def validate(userRequest: RegisterUserRequest): Validated[NonEmptyList[String], RegisterUserRequest] =
    Applicative[Validated[NonEmptyList[String], ?]].map3(validateEmail(userRequest), validateAge(userRequest), validateGender(userRequest))((validUser, _, _) => validUser)

  private def validateAge(userRequest: RegisterUserRequest): Validated[NonEmptyList[String], RegisterUserRequest] =
    Validated.cond(userRequest.age >= minAge && userRequest.age <= maxAge, userRequest, InvalidAge)

  private def validateGender(userRequest: RegisterUserRequest): Validated[NonEmptyList[String], RegisterUserRequest] =
    Validated.cond(allowedGenders.contains(userRequest.gender), userRequest, InvalidGender)

  private def validateEmail(userRequest: RegisterUserRequest): Validated[NonEmptyList[String], RegisterUserRequest] =
    Validated.cond(Option(userRequest.email).nonEmpty &&
      emailRegex.findFirstIn(userRequest.email).isDefined, userRequest, InvalidEmail)
}
