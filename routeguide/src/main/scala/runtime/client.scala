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

import cats.implicits._
import freestyle._
import freestyle.implicits._
import freestyle.async.implicits._
import freestyle.config.implicits._
import freestyle.rpc.client._
import freestyle.rpc.client.implicits._
import freestyle.rpc.client.handlers._
import cats.~>
import io.grpc.ManagedChannel
import routeguide.protocols.RouteGuideService
import routeguide.runtime.handlers.RouteGuideClientHandler

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object client {

  trait Config {

    val channelFor: ManagedChannelFor =
      ConfigForAddress[ChannelConfig.Op]("rpc.client.host", "rpc.client.port")
        .interpret[Try] match {
        case Success(c) => c
        case Failure(e) =>
          e.printStackTrace()
          throw new RuntimeException("Unable to load the client configuration", e)
      }

    val channelConfigList: List[ManagedChannelConfig] = List(UsePlaintext(true))

  }

  trait Implicits extends CommonImplicits with Config {

    val managedChannelInterpreter =
      new ManagedChannelInterpreter[Future](channelFor, channelConfigList)

    implicit def channelMHandler[F[_]]: ChannelM.Op ~> Future =
      new ChannelMHandler[Future] andThen managedChannelInterpreter

    val channel: ManagedChannel = managedChannelInterpreter.build(channelFor, channelConfigList)

    implicit val routeGuideServiceClient: RouteGuideService.Client[Future] =
      RouteGuideService.client[Future](channel)

    implicit val routeGuideClientHandler: RouteGuideClientHandler[Future] =
      new RouteGuideClientHandler[Future]
  }

  object implicits extends Implicits

}
