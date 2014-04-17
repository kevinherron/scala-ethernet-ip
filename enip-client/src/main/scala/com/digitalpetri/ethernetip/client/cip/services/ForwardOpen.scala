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

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath.{InstanceId, ClassId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.services.ForwardOpenService
import com.digitalpetri.ethernetip.cip.services.ForwardOpenService.{ForwardOpenRequest, ForwardOpenResponse}
import com.digitalpetri.ethernetip.cip.structs.{MessageRouterResponse, MessageRouterRequest}
import com.digitalpetri.ethernetip.client.cip.CipResponseException
import io.netty.buffer.{Unpooled, ByteBuf}
import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success, Try}

class ForwardOpen(request: ForwardOpenRequest) extends InvokableService[ForwardOpenResponse] {

  private val requestPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  private val promise = Promise[ForwardOpenResponse]()

  def response: Future[ForwardOpenResponse] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = ForwardOpenService.ServiceCode,
      requestPath = requestPath,
      requestData = ForwardOpenRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    val responseTry = for {
      responseData  <- decodeMessageRouterResponse(data)
      response      <- ForwardOpenResponse.decode(responseData)
    } yield response

    responseTry match {
      case Success(response) => promise.success(response)
      case Failure(ex) => promise.failure(ex)
    }

    None
  }

  private def decodeMessageRouterResponse(buffer: ByteBuf): Try[ByteBuf] = Try {
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

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)


}
