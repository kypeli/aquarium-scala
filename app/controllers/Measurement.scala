package controllers

import models._
import filters._
import repositories._

import play.api._
import play.api.mvc._
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._

import com.mongodb.casbah.Imports._

import java.util.Calendar

class Measurement extends Controller {
  def getAll = Action {
    MongoDBRepository.getCollection("measurements")
      .map {
        coll => 
          Logger.info("Returning measurements.")

          val timeConstraint = Math.floor(Calendar.getInstance().getTimeInMillis() / 1000) - 60*60*24*2;
          val values = coll.find("epoch_timestamp" $gt timeConstraint)
                           .sort(MongoDBObject("epoch_timestamp" -> 1))
          val measurements = for {
            value <- values
            temp = value.as[String]("temperature")
            timestamp = value.as[String]("timestamp")
            epoc = value.as[Number]("epoch_timestamp").intValue
          } yield MeasurementJson(temp, timestamp, epoc)
          
          val response = Json.obj(
              "measurements" -> measurements.toList
            )
          
          MongoDBRepository.close()
          Ok(response)
      }.getOrElse(InternalServerError("Could not connect to DB."))
  }

  def add = (Action andThen AddMeasurementFilter) { request => 
    val json = request.body.asJson.get
    Logger.debug("New request: " + Json.prettyPrint(json))
    
    val newDocument = MongoDBObject(
                                    "temperature" -> (json \ "temperature").as[String],
                                    "timestamp" -> (json \ "timestamp").as[String],
                                    "epoch_timestamp" -> (json \ "epoch_timestamp").as[Int]
                                   )

    MongoDBRepository.getCollection("measurements")
      .map {
        coll => 
          coll.insert(newDocument)
          Logger.debug("New document: " + newDocument.toString)
          MongoDBRepository.close()
          Ok
      }.getOrElse(InternalServerError("Could not connect to DB."))
  }
}
