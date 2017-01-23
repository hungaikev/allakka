package com.learning.chapter1

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

/**
  * Created by hungai on 1/20/17.
  */


class RestApi(system: ActorSystem, timeout: Timeout)
  extends RestRoutes {
  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher

  def createBoxOffice = system.actorOf(BoxOffice.props, BoxOffice.name)
}


trait RestRoutes extends BoxOfficeApi
  with EventMarshalling {

  import StatusCodes._

  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute

  def eventsRoute =
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          // GET /events
          onSuccess(getEvents()) { events =>
            complete(OK, events)
          }
        }
      }
    }

  def eventRoute =
    pathPrefix("events"/ Segment) { event =>
      pathEndOrSingleSlash {
        post {
          //POST /events/:event
          entity(as[EventDescription]) { ed =>
            //Create event using createEvent method that calls BoxOffice actor
            onSuccess(createEvent(event, ed.tickets)) {
              // Completes request with 201 Created when result is a successful case
              case  BoxOffice.EventCreated(event) => complete(Created,event)
              // Completes request with 400 BadRequest if event could not be created
              case  BoxOffice.EventExists =>
                  val err = Error(s"$event event exists already")
                  complete(BadRequest,err)
            }
          }
        } ~
        get {
          // GET /events/:event
          onSuccess(getEvent(event)){
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        } ~
        delete {
          // DELETE /events/:event
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }

  def ticketsRoute =
    pathPrefix("events" / Segment / "tickets") { event =>
      post {
        pathEndOrSingleSlash {
          // POST /events/:event/tickets
          entity(as[TicketRequest]) { request =>  //Unmarshalls JSON tickets into TicketRequest case class
            onSuccess(requestTickets(event, request.tickets)) { tickets =>
              if(tickets.entries.isEmpty) complete(NotFound)  //Responds with 404 NotFound if the tickets aren't available
              else complete(Created, tickets) // Responds with 201 Created marshalling the tickets to a JSON entity
            }
          }
        }
      }
    }


}


/**
  * BoxOfficeApi to wrap all interactions with the BoxOffice actor
  */

trait BoxOfficeApi{
  import BoxOffice._

  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val boxOffice = createBoxOffice()

  def createEvent(event:String,nrOfTickets:Int) =
    boxOffice.ask(CreateEvent(event,nrOfTickets))
    .mapTo[EventResponse]

  def getEvents() = boxOffice.ask(GetEvents).mapTo[Events]

  def getEvent(event:String) = boxOffice.ask(GetEvent(event))
    .mapTo[Option[Event]]

  def cancelEvent(event:String) = boxOffice.ask(CancelEvent(event))
    .mapTo[Option[Event]]

  def requestTickets(event:String,tickets:Int) = boxOffice.ask(GetTickets(event,tickets))
    .mapTo[TicketSeller.Tickets]
}
