package routeguide
package runtime.handlers

import cats.implicits._
import cats.{Monad, MonadError}
import io.grpc.StatusRuntimeException
import journal.Logger
import routeguide.protocols.{Feature, Point, RouteGuideService}

class RouteGuideClientHandler[F[_]: Monad](
    implicit client: RouteGuideService.Client[F],
    M: MonadError[F, Throwable])
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

  override def listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int): F[Unit] = ???

  override def recordRoute(features: List[Feature], numPoints: Int): F[Unit] = ???

  override def routeChat: F[Unit] = ???

}
