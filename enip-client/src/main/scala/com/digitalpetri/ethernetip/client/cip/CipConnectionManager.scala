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
import com.digitalpetri.ethernetip.cip.services.ForwardOpenService
import com.digitalpetri.ethernetip.cip.services.ForwardOpenService.ForwardOpenRequest
import com.digitalpetri.ethernetip.client.cip.services.ForwardOpen
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

  def takeConnection(): Future[CipConnection] = {
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
        if (c.valid) {
          promise.success(c)
          logger.info(s"CipConnection taken: $c")
        } else {
          promise.completeWith(takeConnection())
        }

      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  def releaseConnection(connection: CipConnection) {
    logger.info(s"CipConnection released: $connection")
    
    timeouts.remove(connection.o2tConnectionId).foreach(_ => offerConnection(connection))
  }

  private def offerConnection(connection: CipConnection) {
    val timeout = config.wheelTimer.newTimeout(new TimerTask {
      def run(t: Timeout): Unit = {
        timeouts.remove(connection.o2tConnectionId).foreach {
          to =>
            connection.valid = false
            count.decrementAndGet()
            logger.info(s"CipConnection timed out: $connection")
        }
      }
    }, config.connectionTimeout.length, config.connectionTimeout.unit)

    timeouts += (connection.o2tConnectionId -> timeout)

    queue.offer(connection)
  }

  private def allocateConnection(): Future[CipConnection] = {
    val promise = Promise[CipConnection]()

    val segments = config.connectionPath.segments ++ ForwardOpenService.MessageRouterConnectionPoint.segments
    val connectionPath = PaddedEPath(segments: _*)

    val request = ForwardOpenRequest(
      timeout                 = config.connectionTimeout,
      connectionSerialNumber  = Random.nextInt(),
      vendorId                = 0,
      vendorSerialNumber      = 0,
      connectionPath          = connectionPath,
      o2tNetworkConnectionParameters = ForwardOpenService.DefaultExplicitConnectionParameters,
      t2oNetworkConnectionParameters = ForwardOpenService.DefaultExplicitConnectionParameters)

    val service = new ForwardOpen(request)

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

        logger.info(s"CipConnection allocated: $connection")

        promise.success(connection)

      case Failure(ex) =>
        logger.error(s"Failed to open CipConnection: ${ex.getMessage}")
        promise.failure(ex)
    }

    promise.future
  }

}
