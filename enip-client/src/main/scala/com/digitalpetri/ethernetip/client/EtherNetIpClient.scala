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

import java.util.concurrent.atomic.AtomicLong

import com.codahale.metrics.{Counter, MetricRegistry}
import com.digitalpetri.ethernetip.client.util.ScalaMetricSet
import com.digitalpetri.ethernetip.encapsulation.commands._
import com.digitalpetri.ethernetip.encapsulation.layers.PacketReceiver
import com.digitalpetri.ethernetip.encapsulation.{EipSuccess, EncapsulationPacket}
import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener}
import io.netty.util.{Timeout, TimerTask}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class EtherNetIpClient(config: EtherNetIpClientConfig) extends PacketReceiver with StrictLogging {

  protected val channelManager = new EtherNetIpChannelManager(this, config)

  protected val timeoutCounter = new Counter()

  private val sessionHandle = new AtomicLong(0L)
  private val senderContext = new AtomicLong(0L)
  private val pendingPackets = new TrieMap[Long, Promise[EncapsulationPacket]]()

  private[client] def registerSession(channel: Channel): Future[RegisterSession] = {
    implicit val ec = config.executionContext

    val promise = Promise[RegisterSession]()

    sendCommand(RegisterSession(), Some(channel)).onComplete {
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

  private def sendCommand[T <: Command](command: T, channel: Option[Channel] = None): Future[EncapsulationPacket] = {
    implicit val ec = config.executionContext

    val promise = Promise[EncapsulationPacket]()

    channel match {
      case Some(ch) =>
        write(ch)

      case None =>
        channelManager.getChannel match {
          case Left(fch) => fch.onComplete {
            case Success(ch) => write(ch)
            case Failure(ex) => promise.failure(ex)
          }
          case Right(ch) => write(ch)
        }
    }

    def write(ch: Channel) {
      val context = senderContext.getAndIncrement

      pendingPackets += (context -> promise)

      val timeout = config.wheelTimer.newTimeout(new TimerTask {
        override def run(timeout: Timeout): Unit = {
          pendingPackets.remove(context) match {
            case Some(p) =>
              p.failure(new Exception("timed out waiting for response."))
              timeoutCounter.inc()
            case None => // It arrived just in the nick of time...
          }
        }
      }, config.requestTimeout.length, config.requestTimeout.unit)

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
   * @param packet an [[EncapsulationPacket]].
   */
  override def onPacketReceived(packet: EncapsulationPacket): Unit = {
    packet.status match {
      case EipSuccess =>
        packet.data match {
          case Some(command: SendUnitData) =>
            onUnitDataReceived(command)

          case _ =>
            pendingPackets.remove(packet.senderContext) match {
              case Some(promise) => promise.success(packet)
              case None => logger.error(s"No pending packet for senderContext=${packet.senderContext}")
            }
        }

      case status => logger.error(s"Received encapsulation layer status: $status")
    }
  }

  /**
   * A [[SendUnitData]] command has been decoded.
   * @param command a [[SendUnitData]] command.
   */
  def onUnitDataReceived(command: SendUnitData) {}

  def getMetricSet: ScalaMetricSet = {
    val metrics = Map(metricName("timeout-counter") -> timeoutCounter)

    new ScalaMetricSet(metrics)
  }

  protected def metricName(name: String) = {
    MetricRegistry.name(getClass, config.instanceId.getOrElse(""), name)
  }

  /**
   * @return the status of the underlying connection - Idle, Connecting, or Connected.
   */
  def getStatus: String = channelManager.getStatus

  def disconnect() {
    channelManager.disconnect()
  }

}
