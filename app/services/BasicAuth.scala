package services

import sun.misc.{BASE64Decoder, BASE64Encoder}

object BasicAuth {
  private val Regex = "^Basic (.*)$".r
  private val encoder = new BASE64Encoder()
  private val decoder = new BASE64Decoder()

  def encode(username: String, password: String): String =
    s"Basic ${new String(encoder.encode(s"$username:$password".getBytes))}"

  def decode(header: String): Option[(String, String)] =
    header match {
      case Regex(encoded) =>
        new String(decoder.decodeBuffer(encoded)).split(":") match {
          case Array(user, pass) => Some((user, pass))
          case _                 => None
        }

      case _ => None
    }
}
