/*
 * EtherNet/IP
 * Copyright (C) 2014 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.ethernetip.client.util

import io.netty.util.{Timeout, TimerTask, HashedWheelTimer}
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.concurrent._
import scala.concurrent.duration.Duration

object AsyncQueue {
  private sealed trait State[+T]
  private case object Idle extends State[Nothing]
  private case class Offering[T](q: Queue[T]) extends State[T]
  private case class Polling[T](q: Queue[Promise[T]]) extends State[T]
  private case class Excepting(exc: Throwable) extends State[Nothing]
}

/**
 * An asynchronous FIFO queue. In addition to providing {{offer()}} and {{poll()}}, the queue can be "failed", flushing
 * current pollers.
 */
class AsyncQueue[T](wheelTimer: HashedWheelTimer, executionContext: ExecutionContext) {

  import AsyncQueue._

  private implicit val ec = executionContext

  private val state = new AtomicReference[State[T]](Idle)

  def size: Int = state.get match {
    case Offering(q) => q.size
    case _ => 0
  }

  /**
   * Retrieves and removes the head of the queue, completing the returned future when the element is available.
   */
  @tailrec
  final def poll(timeout: Option[Duration] = None): Future[T] = state.get match {
    case s@Idle =>
      val p: Promise[T] = promise()
      if (state.compareAndSet(s, Polling(Queue(p)))) {
        timeout.foreach(scheduleTimeout(_, p))
        p.future
      } else {
        poll()
      }

    case s@Polling(q) =>
      val p: Promise[T] = promise()
      if (state.compareAndSet(s, Polling(q.enqueue(p)))) {
        timeout.foreach(scheduleTimeout(_, p))
        p.future
      } else {
        poll()
      }

    case s@Offering(q) =>
      val (elem, nextQ) = q.dequeue
      val nextState = if (nextQ.nonEmpty) Offering(nextQ) else Idle
      if (state.compareAndSet(s, nextState)) Future.successful(elem) else poll()

    case Excepting(exc) => Future.failed(exc)
  }

  private def scheduleTimeout(duration: Duration, promise: Promise[T]) {
    val timeout = wheelTimer.newTimeout(new TimerTask {
      override def run(timeout: Timeout): Unit = {
        promise.tryFailure(new TimeoutException(s"timed out after $duration"))
      }
    }, duration.length, duration.unit)

    promise.future.onComplete {
      case _ => if (!timeout.isCancelled) timeout.cancel()
    }
  }

  /**
   * Insert the given element at the tail of the queue.
   */
  @tailrec
  final def offer(elem: T): Unit = state.get match {
    case s@Idle =>
      if (!state.compareAndSet(s, Offering(Queue(elem)))) offer(elem)

    case s@Offering(q) =>
      if (!state.compareAndSet(s, Offering(q.enqueue(elem)))) offer(elem)

    case s@Polling(q) =>
      val (waiter, nextQ) = q.dequeue
      val nextState = if (nextQ.nonEmpty) Polling(nextQ) else Idle

      if (state.compareAndSet(s, nextState)) {
        if (!waiter.trySuccess(elem)) offer(elem)
      } else {
        offer(elem)
      }

    case Excepting(_) => // Drop.
  }

  /**
   * Fail the queue: current and subsequent pollers will be completed with the given exception.
   */
  @tailrec
  final def fail(exc: Throwable): Unit = state.get match {
    case s@Idle =>
      if (!state.compareAndSet(s, Excepting(exc))) fail(exc)

    case s@Polling(q) =>
      if (!state.compareAndSet(s, Excepting(exc))) fail(exc)
      else q.foreach(_.failure(exc))

    case s@Offering(_) =>
      if (!state.compareAndSet(s, Excepting(exc))) fail(exc)

    case Excepting(_) => // Just take the first one.
  }

  override def toString = "AsyncQueue<%s>".format(state.get)

}
