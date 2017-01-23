package com.learning.chapter3

import akka.actor._
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.learning.chapter3.RequestHandler._

import scala.io.StdIn
import com.typesafe.config._
import scala.concurrent.duration._


/**
  * Created by hungai on 1/21/17.
  */
object Main extends App with HealthJsonSupport {

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("simple-rest-system")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val requestHandler = system.actorOf(RequestHandler.props(),"requestHandler")

  //Define the route
  val route: Route  = {

    implicit val timeout = Timeout(20.seconds)

    path("health") {
      get{
        onSuccess(requestHandler.ask(GetHealthRequest)) {
          case response: HealthResponse =>
            complete(response.health)
          case _ => complete(StatusCodes.InternalServerError)
        }
      } ~
      post {
        entity(as[Health]){ statusReport =>
          onSuccess(requestHandler.ask(SetStatusRequest(statusReport))){
            case response:HealthResponse =>
              complete(response.health)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  //Startup, and listen for requests
  val bindingFuture = Http().bindAndHandle(route,host,port)
  println(s"Waiting for requests at http://$host:$port /...\nHit RETURN to terminate")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind())
  system.terminate()

}
