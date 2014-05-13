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

package com.digitalpetri.ethernetip.client.cip

import com.codahale.metrics.Timer
import com.digitalpetri.ethernetip.cip.structs.ConnectedPacket
import com.digitalpetri.ethernetip.client.util.ScalaMetricSet
import com.digitalpetri.ethernetip.client.{EtherNetIpClient, EtherNetIpClientConfig}
import com.digitalpetri.ethernetip.encapsulation.commands.{SendRRData, SendUnitData}
import com.digitalpetri.ethernetip.encapsulation.cpf.CpfPacket
import com.digitalpetri.ethernetip.encapsulation.cpf.items.{UnconnectedDataItem, NullAddressItem, ConnectedDataItem, ConnectedAddressItem}
import com.digitalpetri.ethernetip.util.Buffers
import com.typesafe.scalalogging.slf4j.Logging
import io.netty.buffer.ByteBuf
import io.netty.util.{TimerTask, Timeout}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success}

class CipClient(val config: EtherNetIpClientConfig) extends EtherNetIpClient(config) with CipServiceInvoker with Logging {

  private implicit val executionContext = config.executionContext

  private val connectedRequestTimer = new Timer()
  private val unconnectedRequestTimer = new Timer()

  private val pending = new TrieMap[Short, Promise[ByteBuf]]()
  private val sequenceNumber = new AtomicInteger(0)

  def sendConnectedData(data: ByteBuf, connectionId: Int): Future[ByteBuf] = {
    val promise = Promise[ByteBuf]()
    val packet = ConnectedPacket(nextSequenceNumber())(data)

    val addressItem = ConnectedAddressItem(connectionId)
    val dataItem = {
      val buffer = Buffers.unpooled()
      ConnectedPacket.encode(packet, buffer)
      ConnectedDataItem(buffer)
    }

    pending += (packet.sequenceNumber -> promise)

    val timeout = config.wheelTimer.newTimeout(new TimerTask {
      override def run(timeout: Timeout): Unit = {
        pending.remove(packet.sequenceNumber) match {
          case Some(p) =>
            p.failure(new Exception("timed out waiting for response."))
            timeoutCounter.inc()
          case None => // It arrived just in the nick of time...
        }
      }
    }, config.requestTimeout.length, config.requestTimeout.unit)

    val timerContext = connectedRequestTimer.time()

    sendData(SendUnitData(packet = CpfPacket(List(addressItem, dataItem))))

    promise.future.onComplete {
      case _ =>
        timeout.cancel()
        timerContext.stop()
    }

    promise.future
  }

  def sendUnconnectedData(data: ByteBuf): Future[ByteBuf] = {
    val promise = Promise[ByteBuf]()
    val command = SendRRData(packet = CpfPacket(List(NullAddressItem(), UnconnectedDataItem(data))))

    val timerContext = unconnectedRequestTimer.time()

    sendData(command).onComplete {
      case Success(response) =>
        response.packet.items match {
          case NullAddressItem() :: UnconnectedDataItem(buffer) :: Nil => promise.success(buffer)
          case _ => promise.failure(new Exception(s"unexpected items: ${response.packet.items}"))
        }

      case Failure(ex) => promise.failure(ex)
    }

    promise.future.onComplete {
      case _ => timerContext.stop()
    }

    promise.future
  }

  override def onUnitDataReceived(command: SendUnitData): Unit = {
    command.packet.items match {
      case ConnectedAddressItem(connectionId) :: ConnectedDataItem(buffer) :: Nil =>
        val packet = ConnectedPacket.decode(buffer)

        pending.get(packet.sequenceNumber) match {
          case Some(promise) => promise.success(packet.data)
          case None => logger.debug(s"Received unmatched connected data. connectionId=$connectionId, sequenceNumber=${packet.sequenceNumber}")
        }

      case _ => logger.warn(s"Received unexpected items: ${command.packet.items}")
    }
  }

  private def nextSequenceNumber(): Short =
    sequenceNumber.incrementAndGet().toShort

  override def getMetricSet: ScalaMetricSet = {
    val metrics = Map(
      metricName("connected-request-timer") -> connectedRequestTimer,
      metricName("unconnected-request-timer") -> unconnectedRequestTimer)

    new ScalaMetricSet(super.getMetricSet.metrics ++ metrics)
  }

  override def disconnect(): Unit = {
    // TODO ForwardClose all CipConnections

    super.disconnect()
  }

}

