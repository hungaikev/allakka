package com.learning.chapter4.quiz

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route

import com.learning.chapter4.quiz.resources._
import com.learning.chapter4.quiz.service._

/**
  * Created by hungai on 1/23/17.
  */



trait RestInterface  extends Resources {

  implicit def ec:ExecutionContext

  lazy val questionService = new QuestionService

  val routes: Route = questionRoutes

}


trait Resources extends QuestionResource
