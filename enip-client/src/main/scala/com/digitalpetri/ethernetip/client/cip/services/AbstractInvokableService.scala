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

package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.structs.MessageRouterResponse
import com.digitalpetri.ethernetip.client.cip.CipResponseException
import io.netty.buffer.{ByteBuf, Unpooled}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

abstract class AbstractInvokableService[T] extends InvokableService[T] {

  protected val promise = Promise[T]()

  def response: Future[T] = promise.future

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    val responseTry = for {
      responseData  <- decodeMessageRouterResponse(data)
      response      <- decodeResponse(responseData)
    } yield response

    responseTry match {
      case Success(response) => promise.success(response)
      case Failure(ex) => promise.failure(ex)
    }

    None
  }

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)

  def decodeMessageRouterResponse(buffer: ByteBuf): Try[ByteBuf] = Try {
    MessageRouterResponse.decode(buffer) match {
      case Success(response) =>
        if (response.generalStatus == 0x00) {
          response.data.getOrElse(Unpooled.EMPTY_BUFFER)
        } else {
          throw new CipResponseException(response.generalStatus, response.additionalStatus)
        }
      case Failure(ex) => throw ex
    }
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  def decodeResponse(responseData: ByteBuf): Try[T]

}
