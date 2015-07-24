package repositories

import play.api.Play.current

import com.mongodb.casbah.Imports._

object MongoDBRepository {
  private val mongoUriConf = current.configuration.getString("mongodb.uri")
  private var mongoClient: Option[MongoClient] = None

  def getCollection(collection: String): Option[MongoCollection] = {
    mongoClient = getClient

    for {
      uri <- mongoUriConf
      db <- MongoClientURI(uri).database
      client <- mongoClient
    } yield client(db)(collection)
  }

  def close() = {
    mongoClient.get.close
  }

  private def getClient: Option[MongoClient] = {
    mongoUriConf.map {
      uri => 
        val mongoClientUri = MongoClientURI(uri)
        MongoClient(mongoClientUri)
    }
  }
}

