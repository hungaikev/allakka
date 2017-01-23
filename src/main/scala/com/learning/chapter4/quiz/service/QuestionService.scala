package com.learning.chapter4.quiz.service

import com.learning.chapter4.quiz.entities._
import scala.concurrent.{ExecutionContext,Future}

/**
  * Created by hungai on 1/23/17.
  */
class QuestionService (implicit val ec: ExecutionContext) {

  var questions = Vector.empty[Question]

  def createQuestion(question: Question):Future[Option[String]] = Future {
    questions.find(_.id == question.id) match {
      case Some(q) => None
      case None =>
        questions = questions := question
        Some(question.id)
    }
  }

  def getQuestion(id:String):Future[Option[Question]] = Future {
    questions.find(_.id == id)
  }

  def updateQuestion(id:String,update: QuestionUpdate): Future[Option[Question]] = {
    def updateEntity(question: Question):Question = {
      val title = update.title.getOrElse(question.title)
      val text = update.text.getOrElse(question.text)
      Question(id,title,text)
    }


    getQuestion(id).flatMap { maybeQuestion =>
      maybeQuestion match  {
        case None => Future{None} //No Question Found, Nothing to update
        case Some(question) =>
          val updatedQuestion = updateEntity(question)
          deleteQuestion(id).flatMap { _ =>
            createQuestion(updatedQuestion).map( _ =>
              Some(updatedQuestion)
            )
          }
      }

    }
  }

  def deleteQuestion(id:String):Future[Unit] = Future {
    questions = questions.filterNot(_.id == id)
  }

}
