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

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.services.ForwardOpen
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.ForwardOpenRequest
import com.digitalpetri.ethernetip.client.cip.services.ForwardOpenService
import com.digitalpetri.ethernetip.client.util.AsyncQueue
import com.typesafe.scalalogging.slf4j.Logging
import io.netty.util.{TimerTask, Timeout}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Promise, Future}
import scala.util.{Random, Failure, Success}

trait CipConnectionManager extends Logging {
  this: CipClient =>

  private implicit val executionContext = config.executionContext

  private val count = new AtomicInteger(0)

  private val queue = new AsyncQueue[CipConnection](
    wheelTimer = config.wheelTimer,
    executionContext = config.executionContext)

  private val timeouts = new TrieMap[Int, Timeout]()

  def reserveConnection(): Future[CipConnection] = {
    val promise = Promise[CipConnection]()

    val future = queue.poll(Some(config.timeout))

    if (!future.isCompleted) {
      if (count.incrementAndGet() <= config.connections) {
        allocateConnection().onComplete {
          case Success(connection) => offerConnection(connection)
          case Failure(ex) => count.decrementAndGet()
        }
      } else {
        count.decrementAndGet()
      }
    }

    future.onComplete {
      case Success(c) =>
        if (timeouts.contains(c.o2tConnectionId)) {
          // If `timeouts` contains an entry for this connection then it hasn't timed out.
          logger.trace(s"CipConnection taken: $c")
          promise.success(c)
        } else {
          promise.completeWith(reserveConnection())
        }

      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def releaseConnection(connection: CipConnection) {
    logger.trace(s"CipConnection released: $connection")

    timeouts.remove(connection.o2tConnectionId).foreach(_ => offerConnection(connection))
  }

  private def offerConnection(connection: CipConnection) {
    val timeout = config.wheelTimer.newTimeout(new TimerTask {
      def run(t: Timeout): Unit = {
        timeouts.remove(connection.o2tConnectionId).foreach {
          to =>
            count.decrementAndGet()
            logger.debug(s"CipConnection timed out: $connection")
        }
      }
    }, config.connectionTimeout.length, config.connectionTimeout.unit)

    timeouts += (connection.o2tConnectionId -> timeout)

    queue.offer(connection)
  }

  private def allocateConnection(): Future[CipConnection] = {
    val promise = Promise[CipConnection]()

    val segments = config.connectionPath.segments ++ ForwardOpen.MessageRouterConnectionPoint.segments
    val connectionPath = PaddedEPath(segments: _*)

    val request = ForwardOpenRequest(
      timeout                 = config.connectionTimeout,
      connectionSerialNumber  = Random.nextInt(),
      vendorId                = 0,
      vendorSerialNumber      = 0,
      connectionPath          = connectionPath,
      o2tNetworkConnectionParameters = ForwardOpen.DefaultExplicitConnectionParameters,
      t2oNetworkConnectionParameters = ForwardOpen.DefaultExplicitConnectionParameters)

    val service = new ForwardOpenService(request)

    sendUnconnectedData(service.getRequestData).onComplete {
      case Success(responseData) => service.setResponseData(responseData)
      case Failure(ex) => service.setResponseFailure(ex)
    }

    service.response.onComplete {
      case Success(response) =>
        val connection = CipConnection(
          o2tConnectionId         = response.o2tConnectionId,
          t2oConnectionId         = response.t2oConnectionId,
          serialNumber            = response.connectionSerialNumber,
          originatorVendorId      = response.originatorVendorId,
          originatorSerialNumber  = response.originatorSerialNumber,
          timeout                 = config.connectionTimeout)

        logger.debug(s"CipConnection allocated: $connection")

        promise.success(connection)

      case Failure(ex) =>
        logger.error(s"Failed to open CipConnection: ${ex.getMessage}")
        promise.failure(ex)
    }

    promise.future
  }

}
