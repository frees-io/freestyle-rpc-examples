package routeguide
package handlers

import cats._
import cats.implicits._
import io.grpc.{Status, StatusRuntimeException}
import journal.Logger
import monix.eval.Task
import monix.reactive.Observable
import routeguide.protocols._

import scala.concurrent.duration._

class RouteGuideClientHandler[F[_]: Monad](
    implicit client: RouteGuideService.Client[F],
    M: MonadError[F, Throwable],
    T2F: Task ~> F)
    extends RouteGuideClient.Handler[F] {

  val logger: Logger = Logger[this.type]

  override def getFeature(lat: Int, lon: Int): F[Unit] =
    M.handleErrorWith {
      logger.info(s"*** GetFeature: lat=$lat lon=$lon")
      client
        .getFeature(Point(lat, lon))
        .map { feature: Feature =>
          if (feature.valid)
            logger.info(s"Found feature called '${feature.name}' at ${feature.location.pretty}")
          else logger.info(s"Found no feature at ${feature.location.pretty}")
        }
    } {
      case e: StatusRuntimeException =>
        logger.warn(s"RPC failed:${e.getStatus}", e)
        M.raiseError(e)
    }

  override def listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int): F[Unit] = T2F.apply {
    logger.info(s"*** ListFeatures: lowLat=$lowLat lowLon=$lowLon hiLat=$hiLat hiLon=$hiLon")
    client
      .listFeatures(
        Rectangle(
          lo = Point(lowLat, lowLon),
          hi = Point(hiLat, hiLon)
        ))
      .zipWithIndex
      .map {
        case (feature, i) =>
          logger.info(s"Result #$i: $feature")
      }
      .onErrorHandle {
        case e: StatusRuntimeException =>
          logger.warn(s"RPC failed: ${e.getStatus}", e)
          throw e
      }
      .completedL
  }

  override def recordRoute(features: List[Feature], numPoints: Int): F[Unit] = {
    def takeN: List[Feature] = scala.util.Random.shuffle(features).take(numPoints)

    M.handleErrorWith {
      val points = takeN.map(_.location)
      logger.info(s"*** RecordRoute. Points: ${points.map(_.pretty).mkString(";")}")

      client
        .recordRoute(Observable
          .fromIterable(points))
        .map { summary: RouteSummary =>
          logger.info(
            s"Finished trip with ${summary.point_count} points. Passed ${summary.feature_count} features. " +
              s"Travelled ${summary.distance} meters. It took ${summary.elapsed_time} seconds.")
        }
    } { e: Throwable =>
      logger.warn(s"RecordRoute Failed: ${Status.fromThrowable(e)}", e)
      M.raiseError(e)
    }
  }

  override def routeChat: F[Unit] = T2F.apply {
    logger.info("*** RouteChat")
    client
      .routeChat(
        Observable
          .fromIterable(List(
            RouteNote(message = "First message", location = Point(0, 0)),
            RouteNote(message = "Second message", location = Point(0, 1)),
            RouteNote(message = "Third message", location = Point(1, 0)),
            RouteNote(message = "Fourth message", location = Point(1, 1))
          ))
          .delayOnNext(10.milliseconds)
          .delayOnComplete(1.minute)
          .map { routeNote =>
            logger.info(s"Sending message '${routeNote.message}' at " +
              s"${routeNote.location.latitude}, ${routeNote.location.longitude}")
            routeNote
          }
      )
      .map { note: RouteNote =>
        logger.info(s"Got message '${note.message}' at " +
          s"${note.location.latitude}, ${note.location.longitude}")
      }
      .onErrorHandle {
        case e: Throwable =>
          logger.warn(s"RouteChat Failed: ${Status.fromThrowable(e)}", e)
          throw e
      }
      .completedL
  }

}
