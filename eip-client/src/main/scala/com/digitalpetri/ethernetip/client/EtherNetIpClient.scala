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

package com.digitalpetri.ethernetip.client

import com.digitalpetri.ethernetip.client.util.ChannelManager
import com.digitalpetri.ethernetip.encapsulation.EncapsulationPacket
import com.digitalpetri.ethernetip.encapsulation.commands._
import com.digitalpetri.ethernetip.encapsulation.layers.PacketReceiver
import com.typesafe.scalalogging.slf4j.Logging
import io.netty.channel.{ChannelFuture, ChannelFutureListener, Channel}
import io.netty.util.{Timeout, TimerTask}
import java.util.concurrent.atomic.AtomicLong
import scala.Some
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Promise, Future}
import scala.util.Failure
import scala.util.Success

class EtherNetIpClient(config: EtherNetIpClientConfig) extends PacketReceiver with Logging {

  val channelManager = new ChannelManager(this, config)

  val sessionHandle = new AtomicLong(0L)
  val senderContext = new AtomicLong(0L)
  val pendingPackets = new TrieMap[Long, Promise[EncapsulationPacket]]()

  def registerSession(): Future[RegisterSession] = {
    implicit val ec = config.executionContext

    val promise = Promise[RegisterSession]()

    sendCommand(RegisterSession()).onComplete {
      case Success(packet) =>
        packet.data match {
          case Some(cmd: RegisterSession) =>
            sessionHandle.set(packet.sessionHandle)
            promise.success(cmd)

          case Some(cmd) => promise.failure(new Exception(s"unexpected response: $cmd"))
          case None => promise.failure(new Exception(s"error response: ${packet.status}"))
        }

      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def unRegisterSession(): Future[UnRegisterSession] = {
    sendCommand(UnRegisterSession())

    Future.successful(UnRegisterSession())
  }

  def listIdentity(): Future[ListIdentity] = {
    implicit val ec = config.executionContext

    val promise = Promise[ListIdentity]()

    sendCommand(ListIdentity()).onComplete {
      case Success(packet) =>
        packet.data match {
          case Some(cmd: ListIdentity) => promise.success(cmd)
          case Some(cmd) => promise.failure(new Exception(s"unexpected response: $cmd"))
          case None => promise.failure(new Exception(s"error response: ${packet.status}"))
        }
      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def listInterfaces(): Future[ListInterfaces] = {
    implicit val ec = config.executionContext

    val promise = Promise[ListInterfaces]()

    sendCommand(ListInterfaces()).onComplete {
      case Success(packet) =>
        packet.data match {
          case Some(cmd: ListInterfaces) => promise.success(cmd)
          case Some(cmd) => promise.failure(new Exception(s"unexpected response: $cmd"))
          case None => promise.failure(new Exception(s"error response: ${packet.status}"))
        }
      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def listServices(): Future[ListServices] = {
    implicit val ec = config.executionContext

    val promise = Promise[ListServices]()

    sendCommand(ListServices()).onComplete {
      case Success(packet) =>
        packet.data match {
          case Some(cmd: ListServices) => promise.success(cmd)
          case Some(cmd) => promise.failure(new Exception(s"unexpected response: $cmd"))
          case None => promise.failure(new Exception(s"error response: ${packet.status}"))
        }
      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def sendData(command: SendRRData): Future[SendRRData] = {
    // TODO This should be caller-runs since it probably contains a retained ByteBuf
    implicit val ec = config.executionContext

    val promise = Promise[SendRRData]()

    sendCommand(command).onComplete {
      case Success(packet) =>
        packet.data match {
          case Some(cmd: SendRRData) => promise.success(cmd)
          case Some(cmd) => promise.failure(new Exception(s"unexpected response: $cmd"))
          case None => promise.failure(new Exception(s"error response: ${packet.status}"))
        }
      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def sendData(command: SendUnitData): Future[Unit] = {
    implicit val ec = config.executionContext

    val promise = Promise[Unit]()

    channelManager.getChannel match {
      case Left(fch) => fch.onComplete {
        case Success(ch) => write(ch)
        case Failure(ex) => promise.failure(ex)
      }
      case Right(ch) => write(ch)
    }

    def write(ch: Channel) {
      val packet = EncapsulationPacket(
        commandCode   = command.code.value,
        sessionHandle = sessionHandle.get(),
        senderContext = 0L,
        data          = Some(command))

      ch.writeAndFlush(packet).addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture): Unit = {
          if (future.isSuccess) promise.success(Unit)
          else promise.failure(future.cause())
        }
      })
    }

    promise.future
  }

  private def sendCommand[T <: Command](command: T): Future[EncapsulationPacket] = {
    implicit val ec = config.executionContext

    val promise = Promise[EncapsulationPacket]()

    channelManager.getChannel match {
      case Left(fch) => fch.onComplete {
        case Success(ch) => write(ch)
        case Failure(ex) => promise.failure(ex)
      }
      case Right(ch) => write(ch)
    }

    def write(ch: Channel) {
      val context = senderContext.getAndIncrement

      pendingPackets += (context -> promise)

      val timeout = config.wheelTimer.newTimeout(new TimerTask {
        override def run(timeout: Timeout): Unit = {
          pendingPackets.remove(context) match {
            case Some(p) => p.failure(new Exception("timed out waiting for response."))
            case None => // It arrived just in the nick of time...
          }
        }
      }, config.timeout.length, config.timeout.unit)

      promise.future.onComplete {
        case _ => if (!timeout.isCancelled) timeout.cancel()
      }

      val packet = EncapsulationPacket(
        commandCode   = command.code.value,
        sessionHandle = sessionHandle.get(),
        senderContext = context,
        data          = Some(command))

      ch.writeAndFlush(packet)
    }

    promise.future
  }

  /**
   * An [[EncapsulationPacket]] has been decoded.
   *
   * At this point the current thread is still an event loop thread. Any un-decoded [[io.netty.buffer.ByteBuf]]s
   * contained in the packet must be decoded before leaving this thread.
   *
   *@param packet an [[EncapsulationPacket]].
   */
  override def onPacketReceived(packet: EncapsulationPacket): Unit = {
    packet.data match {
      case Some(command: SendUnitData) =>
        onUnitDataReceived(command)

      case _ =>
        pendingPackets.remove(packet.senderContext) match {
          case Some(promise) => promise.success(packet)
          case None => logger.error(s"No pending packet for senderContext=$senderContext")
        }
    }
  }

  def onUnitDataReceived(command: SendUnitData) {}

}
