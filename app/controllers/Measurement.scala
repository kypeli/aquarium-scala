package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._

import com.mongodb.casbah.Imports._

import java.util.Calendar

class Measurement extends Controller {
  def getAll = Action {
    val mongoUri = current.configuration.getString("mongodb.uri")

    mongoUri.map { 
      uri => 
        Logger.info(s"Getting all measurements.")
        Logger.debug(s"Connecting to Mongo: $uri")

        val mongoUri = MongoClientURI(uri)
        val db = mongoUri.database.get
        val mongo = MongoClient(mongoUri)
        val coll = mongo(db)("measurements")

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

  def add(measurement: String) = Action {
    // TODO
    Ok
  }
}
