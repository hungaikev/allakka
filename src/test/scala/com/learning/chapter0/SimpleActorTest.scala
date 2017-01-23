package com.learning.chapter0


import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender,TestKit}
import org.scalatest.{BeforeAndAfterAll,FunSpecLike}

/**
  * Created by hungai on 1/16/17.
  */
class SimpleActorTest extends TestKit(ActorSystem("SimpleActorSPec"))
  with ImplicitSender
  with FunSpecLike
  with BeforeAndAfterAll{

  import com.learning.chapter0.SimpleActor._

  override def afterAll(): Unit = {
    shutdown(system)
  }

  describe("A Simple Actor") {
    it("should add a number"){
      val simpleActor = system.actorOf(SimpleActor.props(10))
      simpleActor ! Add(5)

      expectMsg(Result(15))
    }
    it("should subtract a number"){
      val simpleActor = system.actorOf(SimpleActor.props(10))
      simpleActor.tell(Subtract(5),self)

      expectMsg(Result(5))
    }
  }

}
