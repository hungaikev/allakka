package com.learning.chapter1


import akka.actor._

import scala.concurrent.Future
import akka.util.Timeout


/**
  * Created by hungai on 1/20/17.
  */

object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"


  case class CreateEvent(name:String,tickets:Int)
  case class GetEvent(name:String)
  case object GetEvents
  case class GetTickets(event:String,tickets:Int)
  case class CancelEvent(name:String)
  case class Event(name:String,tickets:Int)
  case class Events(events:Vector[Event])

  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse

}

class BoxOffice (implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  import context._

  /**
    * Creates a TicketSeller using its context defined in a separate method so
    * its easy to override during testing
    * @param name
    * @return
    */
  def createTicketSeller(name:String) =
    context.actorOf(TicketSeller.props(name),name)

  def receive: Receive = {

    case CreateEvent(name,tickets) =>

      /**
        * A local method that creates the ticket seller, adds the tickets to the ticket seller,
        * and responds with EventCreated
        */
      def create() = {
        val eventTickets = createTicketSeller(name)
        val newTickets = (1 to tickets).map { ticketId =>
          TicketSeller.Ticket(ticketId)
        }.toVector
        eventTickets ! TicketSeller.Add(newTickets)
        sender() ! EventCreated
      }
      //Creates and responds with EventCreated, or responds with EventExists
      context.child(name).fold(create())(_ => sender() ! EventExists)

    case GetTickets(event,tickets) =>
      // Sends an empty Tickets message if the ticket seller couldn't be found
      def notFound() = sender() ! TicketSeller.Tickets(event)

      /**
        * Buys from the found TicketSeller
        * The Buy Message is forwarded to a TicketSeller.Forwarding makes it possible for the BoxOffice to send messages
        * as a proxy for the RestApi. The response of the TicketSeller will go directly to the RestApi
        * @param child
        */
      def buy(child:ActorRef) =
        child.forward(TicketSeller.Buy(tickets))
      // Executes notFound or buys with the found TicketSeller
      context.child(event).fold(notFound())(buy)

    /**
      * GetEvents is going to ask all TicketSellers for the number of tickets they have left and combine all the
      * results into a list of events
      */
    case GetEvents =>
      import akka.pattern.ask
      import akka.pattern.pipe

    //A local method definition for asking all TicketSellers about the events they sell tickets for
    def getEvents = context.children.map { child =>
      self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
    }
      def convertToEvents(f:Future[Iterable[Option[Event]]]) =
        f.map(_.flatten).map(l => Events(l.toVector))
      pipe(convertToEvents(Future.sequence(getEvents))) to sender()

  }

}
