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

import freestyle._
import routeguide.protocols.Feature

@free
trait RouteGuideClient {
  def getFeature(lat: Int, lon: Int): FS[Unit]
  def listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int): FS[Unit]
  def recordRoute(features: List[Feature], numPoints: Int): FS[Unit]
  def routeChat: FS[Unit]
}

object ClientProgram {

  def clientProgram[M[_]](implicit APP: RouteGuideClient[M]): FreeS[M, Unit] = {
    for {
      // Looking for a valid feature
      _ <- APP.getFeature(409146138, -746188906)
      // Feature missing.
      _ <- APP.getFeature(0, 0)
      // Looking for features between 40, -75 and 42, -73.
      _ <- APP.listFeatures(400000000, -750000000, 420000000, -730000000)
      // Record a few randomly selected points from the features file.
      _ <- APP.recordRoute(features, 50)
      // Send and receive some notes.
      _ <- APP.routeChat
    } yield ()
  }
}
