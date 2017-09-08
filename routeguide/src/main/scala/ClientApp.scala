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
import routeguide.protocols.Feature
import routeguide.runtime.client.implicits._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

@free
trait RouteGuideClient {
  def getFeature(lat: Int, lon: Int): FS[Unit]
  def listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int): FS[Unit]
  def recordRoute(features: List[Feature], numPoints: Int): FS[Unit]
  def routeChat: FS[Unit]
}

object ClientApp {

  def clientApp[M[_]](implicit APP: RouteGuideClient[M]): FreeS[M, Unit] = {
    for {
      _ <- APP.getFeature(409146138, -746188906)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    Await.result(clientApp[RouteGuideClient.Op].interpret[Future], Duration.Inf)

    System.in.read()
  }

}
