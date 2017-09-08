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

import routeguide.protocols.{Feature, FeatureDatabase}
import routeguide.codecs._

import scala.concurrent.ExecutionContext
import scala.io.Source

trait CommonImplicits {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

}

object common extends CommonImplicits {

  val features: List[Feature] =
    io.circe.parser.decode[FeatureDatabase](
      Source
        .fromInputStream(getClass.getClassLoader.getResourceAsStream("route_guide_db.json"))
        .mkString) match {
      case Right(fList) => fList.feature
      case Left(e) =>
        println(s"Decoding failure: $e")
        throw e
    }

}
