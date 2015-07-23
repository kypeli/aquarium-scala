package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

object MeasurementJson {
  implicit val measurementWrites: Writes[MeasurementJson] = (
    (JsPath \ "temperature").write[String] and 
    (JsPath \ "timestamp").write[String] and 
    (JsPath \ "epoch_timestamp").write[Int]
  )(unlift(MeasurementJson.unapply))
}

case class MeasurementJson(temperature: String, timestamp: String, timestampEpoc: Int)


