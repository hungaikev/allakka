package com.learning.chapter0

import akka.actor._

/**
  * Created by hungai on 1/20/17.
  */
class Toggle  extends Actor {

  def happy: Receive = {
    case "How are you?" => sender() ! "happy"
      context become sad
  }

  def sad: Receive = {
    case "How are you?" => sender() ! "sad"
      context become happy
  }

  def receive = happy

}
