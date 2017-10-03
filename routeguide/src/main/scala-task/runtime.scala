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
import freestyle.async.{AsyncContext, Proc}
import freestyle.rpc.server._
import routeguide.handlers.RouteGuideServiceHandler
import monix.eval.Task
import monix.cats._
import routeguide.runtime._

import scala.concurrent.Await

trait TaskInstances extends RouteGuide {

  implicit val task2Task: Task ~> Task = new (Task ~> Task) {
    override def apply[A](fa: Task[A]): Task[A] = fa
  }

  implicit val taskAsyncContext: AsyncContext[Task] = new AsyncContext[Task] {
    def runAsync[A](fa: Proc[A]): Task[A] = Task.deferFuture(futureAsyncContext.runAsync(fa))
  }

  implicit val taskCaptureInstance: Capture[monix.eval.Task] =
    new Capture[monix.eval.Task] {
      def capture[A](a: => A): monix.eval.Task[A] = Task.eval(a)
    }

  implicit def taskComonad: Comonad[Task] =
    new Comonad[Task] {
      def extract[A](x: Task[A]): A =
        Await.result(x.runAsync, atMostDuration)

      override def coflatMap[A, B](fa: Task[A])(f: (Task[A]) => B): Task[B] = Task.eval(f(fa))

      override def map[A, B](fa: Task[A])(f: (A) => B): Task[B] =
        fa.map(f)
    }
}

object clientT {

  trait Implicits extends TaskInstances with ClientConf {

    implicit val routeGuideServiceClient: RouteGuideService.Client[Task] =
      RouteGuideService.client[Task](channel)

    implicit val routeGuideClientHandler: RouteGuideClientHandler[Task] =
      new RouteGuideClientHandler[Task]
  }

  object implicits extends Implicits

}

object serverT {

  trait Implicits extends TaskInstances with ServerConf {

    import freestyle.rpc.server.handlers._
    import freestyle.rpc.server.implicits._

    implicit val routeGuideServiceHandler: RouteGuideService.Handler[Task] =
      new RouteGuideServiceHandler[Task]

    val grpcConfigs: List[GrpcConfig] = List(
      AddService(RouteGuideService.bindService[RouteGuideService.Op, Task])
    )

    implicit val grpcServerTaskHandler: GrpcServer.Op ~> Task =
      new GrpcServerHandler[Task] andThen
        new GrpcKInterpreter[Task](getConf(grpcConfigs).server)

  }

  object implicits extends Implicits

}
