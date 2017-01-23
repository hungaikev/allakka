package com.learning.chapter4.quiz.serializers

import java.text.SimpleDateFormat
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{native,DefaultFormats,Formats}

/**
  * Created by hungai on 1/23/17.
  */
trait JsonSupport extends Json4sSupport {
  implicit val serialization = native.Serialization

  implicit def json4sFormats:Formats = customDateFormat ++ JodaTimeSerializers.all ++ CustomSerializers.all

  val customDateFormat = new DefaultFormats {
    override def dateFormatter: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
  }

}
