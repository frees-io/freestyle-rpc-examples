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
import journal.Logger
import monix.eval.Task
import routeguide.protocols.{Feature, FeatureDatabase}
import routeguide.codecs._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

trait CommonImplicits {

  val logger: Logger = Logger[this.type]

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

  implicit val task2Future: Task ~> Future = new (Task ~> Future) {
    override def apply[A](fa: Task[A]): Future[A] = {
      logger.info("Running the Task as Future...")
      fa.runAsync.recover {
        case e: Throwable =>
          logger.error(s"An error has occurred running Task to Future", e)
          throw e
      }
    }
  }

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
