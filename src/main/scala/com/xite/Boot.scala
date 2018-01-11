package com.xite

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.xite.actor.{UserSupervisorActor, VideoManagerActor}
import com.xite.http.route.{ActionRoute, RegistrationRoute}
import com.xite.service.{UserService, UserServiceImpl}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Random

object Boot {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("recommendation-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val config = ConfigFactory.load()

    implicit val actorResponseTimeout = Timeout(Duration.fromNanos(config.getDuration("akka.actor.timeout").toNanos))

    val movieIds = getListOfVideos(10)

    val videoManagerActor = system.actorOf(VideoManagerActor.props(movieIds), "videoManager")
    val userSupervisorActor = system.actorOf(UserSupervisorActor.props(videoManagerActor, actorResponseTimeout), "userSupervisor")

    val userService = new UserServiceImpl(userSupervisorActor)
    val bindingFuture = Http().bindAndHandle(getRoutes(userService), "localhost", 8085)

    println(s"Server online at http://localhost:8085/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  private def getListOfVideos(size: Int): List[Long] = {
    (1 to size).map(_ => Random.nextInt(10000).toLong).toList
  }

  private def getRoutes(userService: UserService)(implicit executionContext: ExecutionContext): Route = {
    val registrationRoute = new RegistrationRoute(userService).route
    val actionRoute = new ActionRoute(userService).route
    registrationRoute ~ actionRoute
  }
}
