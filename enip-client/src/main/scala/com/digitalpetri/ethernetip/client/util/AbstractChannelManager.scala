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

import io.netty.channel.{ChannelFutureListener, ChannelFuture, Channel}
import io.netty.util.concurrent.GenericFutureListener
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent._
import scala.util.{Failure, Success}

object AbstractChannelManager {
  private sealed trait State
  private case object Idle extends State
  private case class Connecting(p: Promise[Channel]) extends State
  private case class Connected(channel: Channel) extends State
}

abstract class AbstractChannelManager {

  import AbstractChannelManager._

  private val state = new AtomicReference[State](Idle)

  def getChannel: Either[Future[Channel], Channel] = {
    state.get match {
      case s@Idle =>
        val p: Promise[Channel] = promise()
        val nextState = Connecting(p)
        if (state.compareAndSet(s, nextState)) Left(connect(nextState, p)) else getChannel

      case s@Connecting(p) =>
        Left(p.future)

      case s@Connected(channel) =>
        Right(channel)
    }
  }

  private def connect(expectedState: State, channelPromise: Promise[Channel]): Future[Channel] = {
    val promise = Promise[Channel]()

    channelPromise.future.onComplete {
      case Success(ch) =>
        preConnectedState(ch).onComplete {
          case _ => moveToConnectedState()
        }

        def moveToConnectedState() {
          ch.closeFuture().addListener(new ChannelFutureListener {
            override def operationComplete(future: ChannelFuture): Unit = state.set(Idle)
          })

          state.set(Connected(ch))
          promise.success(ch)

          postConnectedState()
        }

      case Failure(ex) =>
        state.compareAndSet(expectedState, Idle)
        promise.failure(ex)
    }

    connect(channelPromise)

    promise.future
  }

  /** Make a connection, completing the Promise with the resulting Channel. */
  def connect(channelPromise: Promise[Channel]): Unit

  /** The channel is open and the state is soon to be Connected; maybe do something? */
  def preConnectedState(channel: Channel): Future[Any] = { Future.successful(Unit) }

  /** The state is now Connected; maybe do something? */
  def postConnectedState(): Unit = {}

  /** ExecutionContext to run completion callbacks on. */
  implicit val executionContext: ExecutionContext

  def getStatus: String = state.get match {
    case s@Idle           => "Idle"
    case s@Connecting(_)  => "Connecting"
    case s@Connected(_)   => "Connected"
  }

  def disconnect(): Unit = {
    state.get match {
      case s@Connecting(p) => p.future.onSuccess { case ch => ch.close() }
      case s@Connected(ch) => ch.close()
      case s@Idle => // No-op
    }
  }

}
