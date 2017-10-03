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

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait RouteGuide {

  val logger: Logger = Logger[this.type]

  protected val atMostDuration: FiniteDuration = 10.seconds

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

  implicit val task2Future: Task ~> Future = new (Task ~> Future) {
    override def apply[A](fa: Task[A]): Future[A] = {
      logger.info(s"${Thread.currentThread().getName} Running the Task as Future...")
      fa.timeout(60.seconds).runAsync
    }
  }

  implicit val future2Task: Future ~> Task = new (Future ~> Task) {
    override def apply[A](fa: Future[A]): Task[A] = {
      logger.info(s"${Thread.currentThread().getName} Deferring Future to Task...")
      Task.deferFuture(fa)
    }
  }
}

object RouteGuide extends RouteGuide
