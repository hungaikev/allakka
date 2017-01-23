package com.learning.chapter2

import akka.actor._

/**
  * Created by hungai on 1/21/17.
  */

case class SensorDataMessage(data: Seq[String])
case class ParsedDataMessage(data: Map[String, Int])
case class LocationData(location:String,data:Map[String,Int])
case class QueryMessage()
case class LocationDataMessage(location:String,data:Map[String,Int])

class DataParserActor extends Actor {

  def receive: Receive = {
    case SensorDataMessage(data) =>
      val parsedData = Map(
        "car" -> data.count(_ == "motocycle"),
        "bus" -> data.count(_ == "bus")
      )
      sender() ! ParsedDataMessage(parsedData)
      context.stop(self)
    case _ => println("DataParserActor got unknown message")
  }


}

class LocationActor (location:String) extends Actor {

  private var carCount = 0
  private var motorcycleCount = 0
  private var busCount = 0

  def receive = {
    case msg: SensorDataMessage =>
      context.actorOf(Props[DataParserActor]) ! msg
    case ParsedDataMessage(data) =>
      carCount += data("car")
      motorcycleCount += data("motorcycle")
      busCount += data("bus")
    case QueryMessage() =>
      val data = Map(
        "car" -> carCount,
        "motorcycle" -> motorcycleCount,
        "bus" -> busCount
      )
      sender ! LocationDataMessage(location = location, data = data)
    case _ => println("LocationActor got unknown message")
  }
}

class QueryActor(locationActors: Seq[ActorRef]) extends Actor {
  private val actorCount = locationActors.size
  private var remainingACtors = actorCount
  private var allData:Map[String,Map[String,Int]] = Map()

  private var inquirer: Option[ActorRef] = None

  def receive = {
    case QueryMessage() =>
      locationActors foreach( _ ! QueryMessage())
      inquirer = Some(sender())

    case LocationDataMessage(location,data) =>
      allData = allData updated(location,data)
      remainingACtors -= 1
      if(remainingACtors == 0) {
        val response :Seq[LocationData] =
          allData.keys.map{ loc =>
            LocationData(loc,allData(loc))
          }.toSeq
        inquirer.map(_ ! response)
        context.stop(self)
      }
    case _ => println("Query Actor received unknown message")
  }
}
