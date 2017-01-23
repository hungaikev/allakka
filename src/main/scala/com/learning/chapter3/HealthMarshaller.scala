package com.learning.chapter3

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.learning.chapter3.RequestHandler.Health
import spray.json.DefaultJsonProtocol

/**
  * Created by hungai on 1/21/17.
  */


trait HealthJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat = jsonFormat2(Health)
}

