package com.learning.chapter4.quiz.serializers

import java.sql.Timestamp
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JInt,JNull}
/**
  * Created by hungai on 1/23/17.
  */

object CustomSerializers {

  val all = List()
}

case object CustomTimeStampSerializer extends CustomSerializer[Timestamp]( format =>
  ({
    case JInt(x) => new Timestamp(x.longValue * 1000)
    case JNull => null
  },
    {
      case date:Timestamp => JInt(date.getTime / 1000)
    }
    ))
