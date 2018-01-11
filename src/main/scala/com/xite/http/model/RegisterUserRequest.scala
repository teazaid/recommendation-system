package com.xite.http.model

import com.xite.model.{Age, Email, Gender, UserName}

final case class RegisterUserRequest(userName: UserName, email: Email, age: Age, gender: Gender)
