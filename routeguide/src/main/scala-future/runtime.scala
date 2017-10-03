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

import cats.implicits._
import freestyle._
import freestyle.implicits._
import freestyle.async.implicits._
import routeguide.handlers.RouteGuideClientHandler
import routeguide.protocols.RouteGuideService

import cats.{~>, Comonad}
import freestyle.rpc.server._
import routeguide.handlers.RouteGuideServiceHandler
import routeguide.runtime._

import scala.concurrent.{Await, ExecutionContext, Future}

trait FutureInstances extends RouteGuide {

  implicit def futureComonad(implicit ec: ExecutionContext): Comonad[Future] =
    new Comonad[Future] {
      def extract[A](x: Future[A]): A =
        Await.result(x, atMostDuration)

      override def coflatMap[A, B](fa: Future[A])(f: (Future[A]) => B): Future[B] = Future(f(fa))

      override def map[A, B](fa: Future[A])(f: (A) => B): Future[B] =
        fa.map(f)
    }
}

object clientF {

  trait Implicits extends FutureInstances with ClientConf {

    implicit val routeGuideServiceClient: RouteGuideService.Client[Future] =
      RouteGuideService.client[Future](channel)

    implicit val routeGuideClientHandler: RouteGuideClientHandler[Future] =
      new RouteGuideClientHandler[Future]
  }

  object implicits extends Implicits

}

object serverF {

  trait Implicits extends FutureInstances with ServerConf {

    import freestyle.rpc.server.handlers._
    import freestyle.rpc.server.implicits._

    implicit val routeGuideServiceHandler: RouteGuideService.Handler[Future] =
      new RouteGuideServiceHandler[Future]

    val grpcConfigs: List[GrpcConfig] = List(
      AddService(RouteGuideService.bindService[RouteGuideService.Op, Future])
    )

    implicit val grpcServerHandler: GrpcServer.Op ~> Future =
      new GrpcServerHandler[Future] andThen
        new GrpcKInterpreter[Future](getConf(grpcConfigs).server)
  }

  object implicits extends Implicits

}
