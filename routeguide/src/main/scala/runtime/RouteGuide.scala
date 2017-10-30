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

import cats.~>
import cats.implicits._
import freestyle.async.implicits._
import freestyle.rpc.server._
import routeguide.handlers.RouteGuideServiceHandler
import routeguide.protocols.RouteGuideService

import scala.concurrent.{ExecutionContext, Future}

trait RouteGuideEC {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

}

trait RouteGuide extends RouteGuideEC

object server {

  trait Implicits extends RouteGuide with ServerConf {

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
