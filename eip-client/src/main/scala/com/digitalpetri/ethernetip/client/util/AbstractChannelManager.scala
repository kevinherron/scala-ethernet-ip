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

import io.netty.channel.{ChannelFuture, Channel}
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

  private[this] val state = new AtomicReference[State](Idle)

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

  private[this] def connect(expectedState: State, channelPromise: Promise[Channel]): Future[Channel] = {
    channelPromise.future.onComplete {
      case Success(ch) =>
        if (state.compareAndSet(expectedState, Connected(ch))) {
          ch.closeFuture().addListener(new GenericFutureListener[ChannelFuture] {
            def operationComplete(future: ChannelFuture) {
              state.set(Idle)
            }
          })
        }

      case Failure(ex) => state.compareAndSet(expectedState, Idle)
    }

    connect(channelPromise)

    channelPromise.future
  }

  /**
   * Make a connection, completing the Promise with the resulting Channel.
   */
  def connect(channelPromise: Promise[Channel]): Unit

  /**
   * ExecutionContext to run completion callbacks on.
   */
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
