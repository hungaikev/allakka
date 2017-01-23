package com.learning.chapter4.quiz.routing

import akka.http.scaladsl.marshalling.{ToResponseMarshaller,ToResponseMarshallable}

import scala.concurrent.{ExecutionContext,Future}
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.{Directives,Route}

import com.learning.chapter4.quiz.serializers.JsonSupport

/**
  * Created by hungai on 1/23/17.
  */
trait MyResource extends Directives with JsonSupport {

  implicit def ec:ExecutionContext

  def completeWithLocationHeader[T](resourceId: Future[Option[T]], ifDefinedStatus: Int, ifEmptyStatus: Int): Route =
    onSuccess(resourceId) {
      case Some(t) => completeWithLocationHeader(ifDefinedStatus, t)
      case None => complete(ifEmptyStatus, None)
    }

  def completeWithLocationheader[T](status:Int, resourceId:T):Route =
    extractRequestContext { requestContext =>
      val request = requestContext.request
      val location = request.uri.copy(path=request.uri.path/resourceId.toString)
      respondWithHeader(Location(location)) {
        complete(status,None)
      }

    }

  def complete[T: ToResponseMarshaller](resource:Future[Option[T]]): Route =
    onSuccess(resource){
      case Some(t) => complete(ToResponseMarshallable(t))
      case None => complete(404,None)
    }

  def complete(resource:Future[Unit]):Route = onSuccess(resource) { complete(204,None)}

}
