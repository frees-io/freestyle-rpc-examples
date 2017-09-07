/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routeguide
package runtime

import java.util.concurrent.atomic.AtomicReference

import cats.{~>, Comonad}
import routeguide.protocols._
import routeguide.codecs._
import monix.reactive.Observable

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

object server extends CommonImplicits {

  import io.circe.parser.decode

  val features: List[Feature] =
    decode[FeatureDatabase](
      Source
        .fromInputStream(getClass.getClassLoader.getResourceAsStream("route_guide_db.json"))
        .mkString) match {
      case Right(fList) => fList.feature
      case Left(e) =>
        println(s"Decoding failure: $e")
        throw e
    }

  class RouteGuideHandler extends RouteGuideService.Handler[Future] {

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

  trait Implicits {

    import cats.implicits._
    import freestyle.rpc.server._
    import freestyle.rpc.server.handlers._
    import freestyle.rpc.server.implicits._
    import freestyle.rpc.client.implicits._

    implicit val finiteDuration: FiniteDuration = 5.seconds

    implicit def futureComonad(
        implicit ec: ExecutionContext,
        atMost: FiniteDuration): Comonad[Future] =
      new Comonad[Future] {
        def extract[A](x: Future[A]): A =
          Await.result(x, atMost)

        override def coflatMap[A, B](fa: Future[A])(f: (Future[A]) => B): Future[B] = Future(f(fa))

        override def map[A, B](fa: Future[A])(f: (A) => B): Future[B] =
          fa.map(f)
      }

    implicit val routeGuideServiceHandler: RouteGuideService.Handler[Future] =
      new RouteGuideHandler

    val grpcConfigs: List[GrpcConfig] = List(
      AddService(RouteGuideService.bindService[RouteGuideService.Op, Future])
    )

    val conf: ServerW = ServerW(50051, grpcConfigs)

    implicit val grpcServerHandler: GrpcServer.Op ~> Future =
      new GrpcServerHandler[Future] andThen
        new GrpcKInterpreter[Future](conf.server)

  }

  object implicits extends Implicits

}
