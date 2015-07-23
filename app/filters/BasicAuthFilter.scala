package filters

import scala.concurrent._

import play.api.mvc._
import play.api.mvc.Results._

import sun.misc.BASE64Decoder

object AddMeasurementFilter extends ActionFilter[Request] {
  private lazy val username = "someUsername"
  private lazy val password = "somePassword"
  //need the space at the end
  private lazy val basicSt = "basic " 

  def filter[A](request: Request[A]) = Future.successful {
    request.headers.get("Authorization").map {
      basicAuth => 
        decodeBasicAuth(basicAuth) match {
          case Some((user, passwd)) => {
            if (username == user && passwd == password) {
              None
            } else {
              Some(Unauthorized)
            }
          }
          case _ => Some(Unauthorized)
        }
    }.getOrElse(Some(Unauthorized))
  }

  private def decodeBasicAuth(auth: String): Option[(String, String)] = {
    if (auth.length() < basicSt.length()) {
      return None
    }
    val basicReqSt = auth.substring(0, basicSt.length())
    if (basicReqSt.toLowerCase() != basicSt) {
      return None
    }
    val basicAuthSt = auth.replaceFirst(basicReqSt, "")
    val decoder = new BASE64Decoder()
    val decodedAuthSt = new String(decoder.decodeBuffer(basicAuthSt), "UTF-8")
    val usernamePassword = decodedAuthSt.split(":")
    if (usernamePassword.length >= 2) {
      //account for ":" in passwords
      return Some(usernamePassword(0), usernamePassword.splitAt(1)._2.mkString)
    }
    None
  }
}
