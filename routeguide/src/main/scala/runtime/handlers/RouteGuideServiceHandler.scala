package routeguide
package runtime.handlers

import java.util.concurrent.atomic.AtomicReference

import monix.reactive.Observable
import routeguide.protocols._
import routeguide.runtime.common._

import scala.concurrent.Future
import scala.concurrent.duration.NANOSECONDS

class RouteGuideServiceHandler extends RouteGuideService.Handler[Future] {

  // AtomicReference as an alternative to ConcurrentMap<Point, List<RouteNote>>?
  private val routeNotes: AtomicReference[Map[Point, List[RouteNote]]] =
    new AtomicReference[Map[Point, List[RouteNote]]](Map.empty)

  override protected[this] def getFeature(point: protocols.Point): Future[Feature] =
    Future.successful(point.findFeatureIn(features))

  override protected[this] def listFeatures(
      rectangle: protocols.Rectangle): Future[Observable[Feature]] = {
    val left   = Math.min(rectangle.lo.longitude, rectangle.hi.longitude)
    val right  = Math.max(rectangle.lo.longitude, rectangle.hi.longitude)
    val top    = Math.max(rectangle.lo.latitude, rectangle.hi.latitude)
    val bottom = Math.min(rectangle.lo.latitude, rectangle.hi.latitude)

    val observable = Observable.fromIterable(
      features.filter { feature =>
        val lat = feature.location.latitude
        val lon = feature.location.longitude
        feature.valid && lon >= left && lon <= right && lat >= bottom && lat <= top

      }
    )

    Future.successful(observable)
  }

  override protected[this] def recordRoute(
      points: Observable[protocols.Point]): Future[RouteSummary] = {
    points
      .foldLeftL((RouteSummary(0, 0, 0, 0), None: Option[Point], System.nanoTime())) {
        case ((routeSummary, previous, startTime), point) =>
          // For each point after the first, add the incremental distance from the previous point to
          // the total distance value.
          val counter  = if (point.findFeatureIn(features).valid) 1 else 0
          val distance = previous.map(calcDistance(_, point)) getOrElse 0
          val updatedRouteSummary: RouteSummary = routeSummary.copy(
            point_count = routeSummary.point_count + 1,
            feature_count = routeSummary.feature_count + counter,
            distance = routeSummary.distance + distance,
            elapsed_time = NANOSECONDS.toSeconds(System.nanoTime() - startTime).toInt
          )
          (updatedRouteSummary, Some(point), startTime)
      }
      .map(_._1)
      .runAsync
  }

  override protected[this] def routeChat(
      routeNotes: Observable[protocols.RouteNote]): Future[Observable[RouteNote]] =
    Future.successful {
      routeNotes flatMap { note =>
        addNote(note)
        Observable.fromIterable(getOrCreateNotes(note.location))
      }
    }

  private[this] def addNote(note: RouteNote): Unit =
    routeNotes.updateAndGet { notes: Map[Point, List[RouteNote]] =>
      val newRouteNotes = notes.getOrElse(note.location, Nil) :+ note
      notes + (note.location -> newRouteNotes)
    }

  private[this] def getOrCreateNotes(point: Point): List[RouteNote] =
    routeNotes.get.getOrElse(point, Nil)
}
