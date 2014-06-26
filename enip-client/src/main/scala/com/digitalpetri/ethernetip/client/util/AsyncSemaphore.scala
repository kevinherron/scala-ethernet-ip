package com.digitalpetri.ethernetip.client.util

import java.util
import java.util.concurrent.RejectedExecutionException

import scala.concurrent.{Future, Promise}

/**
 * An AsyncSemaphore is a traditional semaphore but with asynchronous
 * execution. Grabbing a permit returns a Future[Permit].
 */
class AsyncSemaphore protected (initialPermits: Int, maxWaiters: Option[Int]) {

  import com.digitalpetri.ethernetip.client.util.AsyncSemaphore._

  def this(initialPermits: Int = 0) = this(initialPermits, None)
  def this(initialPermits: Int, maxWaiters: Int) = this(initialPermits, Some(maxWaiters))
  require(maxWaiters.getOrElse(0) >= 0)

  private val waitQueue = new util.ArrayDeque[Promise[Permit]]
  private var availablePermits = initialPermits

  private class SemaphorePermit extends Permit {
    /**
     * Indicate that you are done with your Permit.
     */
    override def release() {
      val p: Promise[Permit] = AsyncSemaphore.this.synchronized {
        val next = waitQueue.pollFirst()
        if (next == null) availablePermits += 1
        next
      }

      if (p != null) p.success(new SemaphorePermit)
    }
  }

  def numWaiters: Int = synchronized(waitQueue.size)
  def numPermitsAvailable: Int = synchronized(availablePermits)

  /**
   * Acquire a Permit, asynchronously. Be sure to permit.release() in a 'finally'
   * block of your onSuccess() callback.
   *
   * Interrupting this future is only advisory, and will not release the permit
   * if the future has already been satisfied.
   *
   * @return a Future[Permit] when the Future is satisfied, computation can proceed,
   * or a Future.Exception[RejectedExecutionException] if the configured maximum number of waitQueue
   * would be exceeded.
   */
  def acquire(): Future[Permit] = {
    synchronized {
      if (availablePermits > 0) {
        availablePermits -= 1
        Future.successful(new SemaphorePermit)
      } else {
        maxWaiters match {
          case Some(max) if waitQueue.size >= max =>
            MaxWaitersExceededException

          case _ =>
            val promise = Promise[Permit]()
            waitQueue.addLast(promise)
            promise.future
        }
      }
    }
  }

}

object AsyncSemaphore {
  private val MaxWaitersExceededException =
    Future.failed(new RejectedExecutionException("max waiters exceeded"))

  trait Permit {
    def release()
  }
}


