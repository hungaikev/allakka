package com.learning.chapter0


import akka.actor.{Actor, Props}

/**
  * Created by hungai on 1/16/17.
  */

object SimpleActor {
  def props(initialValue:Int):Props = Props(classOf[SimpleActor],initialValue)

  case class Add(value:Int)
  case class Subtract(value:Int)
  case class Result(result:Int)
}
class SimpleActor(initialValue:Int) extends Actor{

  import SimpleActor._

  var total = initialValue

  def receive: Receive = {
    case Add(value) =>
      total = total + value
      sender() ! Result(total)
    case Subtract(value) =>
      total = total - value
      sender() ! Result(total)
  }

}
