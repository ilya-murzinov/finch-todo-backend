package todobackend

import cats.implicits._
import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Duration

trait Filters {

  private[this] val policy: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("POST", "GET", "OPTIONS", "DELETE", "PATCH")),
    allowsHeaders = _ => Some(Seq(
      "Content-Type",
      "Cache-Control",
      "Content-Language",
      "Expires",
      "Last-Modified",
      "Pragma",
      "X-Requested-With",
      "Origin",
      "Accept")),
    maxAge = Duration.fromSeconds(3600).some,
  )

  val corsFilter = new Cors.HttpFilter(policy)
}
