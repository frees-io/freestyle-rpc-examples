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
import freestyle.config.implicits._
import freestyle.rpc.client._
import io.grpc.ManagedChannel

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ClientConf {

  val channelFor: ManagedChannelFor =
    ConfigForAddress[ChannelConfig.Op]("rpc.client.host", "rpc.client.port")
      .interpret[Try] match {
      case Success(c) => c
      case Failure(e) =>
        e.printStackTrace()
        throw new RuntimeException("Unable to load the client configuration", e)
    }

  val channelConfigList: List[ManagedChannelConfig] = List(UsePlaintext(true))

  val managedChannelInterpreter =
    new ManagedChannelInterpreter[Future](channelFor, channelConfigList)

  val channel: ManagedChannel = managedChannelInterpreter.build(channelFor, channelConfigList)

}
