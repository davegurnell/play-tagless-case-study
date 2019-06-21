package controllers

import play.api.mvc.{Request, WrappedRequest}

case class AuthRequest[A](
  username: String,
  request: Request[A]
) extends WrappedRequest[A](request)
