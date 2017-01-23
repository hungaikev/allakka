package com.learning.chapter4.quiz.resources

import akka.http.scaladsl.server.Route

import com.learning.chapter4.quiz.entities._
import com.learning.chapter4.quiz.routing._
import com.learning.chapter4.quiz.service._

/**
  * Created by hungai on 1/23/17.
  */
trait QuestionResource extends MyResource {

  val questionService : QuestionService

  def questionRoutes:Route = pathPrefix("questions"){
    pathEnd {
      post {
        entity(as[Question]) { question =>
          completeWithLocationHeader(
            resourceId = questionService.createQuestion(question),
            ifDefinedStatus = 201, ifEmptyStatus = 409)
        }
      }
    } ~
    path (Segment) { id =>
      get {
        complete(questionService.getQuestion(id))
      }~
      put {
        entity(as[QuestionUpdate]) { update =>
          complete(questionService.updateQuestion(id,update))
        }
      }~
      delete {
        complete(questionService.deleteQuestion(id))
      }

    }
  }

}
