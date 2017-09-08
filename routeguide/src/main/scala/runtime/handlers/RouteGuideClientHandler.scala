package routeguide
package runtime.handlers

import cats._
import cats.implicits._
import io.grpc.StatusRuntimeException
import journal.Logger
import monix.eval.Task
import routeguide.protocols._

class RouteGuideClientHandler[F[_]: Monad](
    implicit client: RouteGuideService.Client[F],
    M: MonadError[F, Throwable],
    T2F: Task ~> F)
    extends RouteGuideClient.Handler[F] {

  val logger: Logger = Logger[this.type]

  override def getFeature(lat: Int, lon: Int): F[Unit] =
    M.handleErrorWith {
      val attempt: F[Unit] = M.catchNonFatal {
        logger.info(s"*** GetFeature: lat=$lat lon=$lon")
        client
          .getFeature(Point(lat, lon))
          .map { feature: Feature =>
            if (feature.valid)
              logger.info(s"Found feature called '${feature.name}' at ${feature.location.pretty}")
            else logger.info(s"Found no feature at ${feature.location.pretty}")
          }
      }
      attempt
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

  override def recordRoute(features: List[Feature], numPoints: Int): F[Unit] = ???

  override def routeChat: F[Unit] = ???

}
