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

import cats.{~>, Comonad}
import freestyle.rpc.server._
import routeguide.protocols._
import routeguide.runtime.handlers.RouteGuideServiceHandler

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object server {

  import common._

  trait Instances {

    private[this] val atMostDuration: FiniteDuration = 5.seconds

    implicit def futureComonad(implicit ec: ExecutionContext): Comonad[Future] =
      new Comonad[Future] {
        def extract[A](x: Future[A]): A =
          Await.result(x, atMostDuration)

        override def coflatMap[A, B](fa: Future[A])(f: (Future[A]) => B): Future[B] = Future(f(fa))

        override def map[A, B](fa: Future[A])(f: (A) => B): Future[B] =
          fa.map(f)
      }

  }

  trait Config extends Instances {

    import cats.implicits._
    import freestyle.implicits._
    import freestyle.config.implicits._

    implicit val routeGuideServiceHandler: RouteGuideService.Handler[Future] =
      new RouteGuideServiceHandler

    val grpcConfigs: List[GrpcConfig] = List(
      AddService(RouteGuideService.bindService[RouteGuideService.Op, Future])
    )

    val conf: ServerW =
      BuildServerFromConfig[ServerConfig.Op]("rpc.server.port", grpcConfigs)
        .interpret[Try] match {
        case Success(c) => c
        case Failure(e) =>
          e.printStackTrace()
          throw new RuntimeException("Unable to load the server configuration", e)
      }

  }

  trait Implicits extends CommonImplicits with Config {

    import freestyle.rpc.server.handlers._
    import freestyle.rpc.server.implicits._

    implicit val grpcServerHandler: GrpcServer.Op ~> Future =
      new GrpcServerHandler[Future] andThen
        new GrpcKInterpreter[Future](conf.server)

  }

  object implicits extends Implicits

}
