package com.learning.chapter0


import akka.actor.{Actor, Props}

/**
  * Created by hungai on 1/19/17.
  */
object BankAccount {

  case class Deposit(amount:BigInt){
    require(amount > 0)
  }

  case class Withdraw(amount:BigInt){
    require(amount > 0)
  }

  case object Done
  case object Failed

}

class BankAccount extends Actor {
  import  BankAccount._

  var balance = BigInt(0)

  override def receive: Receive = {
    case Deposit(amount) => balance += amount
      sender() ! Done
    case Withdraw(amount) if amount <= balance => balance -= amount
      sender() ! Done
    case _ => sender() ! Failed
  }
}
