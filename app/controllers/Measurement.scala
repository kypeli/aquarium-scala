package controllers

import models._
import filters._

import play.api._
import play.api.mvc._
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._

import com.mongodb.casbah.Imports._

import java.util.Calendar

class Measurement extends Controller {
  def getAll = Action {
    measurementsCollection.map {
      coll => 
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
    measurementsCollection.map {
      coll => 
        coll.insert(newDocument)
        Logger.debug("New document: " + newDocument.toString)
        Ok
    }.getOrElse(InternalServerError("Could not connect to DB."))
  }

  private def measurementsCollection = {
    val mongoUri = current.configuration.getString("mongodb.uri")

    mongoUri.map { 
      uri => 
        Logger.info(s"Getting MongoDB collection 'measurements'.")

        val mongoUri = MongoClientURI(uri)
        val db = mongoUri.database.get
        val mongo = MongoClient(mongoUri)
        val coll = mongo(db)("measurements")
        coll
    }
  }
}
