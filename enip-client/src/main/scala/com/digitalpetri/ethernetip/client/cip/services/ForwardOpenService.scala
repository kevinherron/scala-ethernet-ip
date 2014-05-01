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
import com.digitalpetri.ethernetip.cip.services.ForwardOpen
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.{ForwardOpenRequest, ForwardOpenResponse}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import io.netty.buffer.ByteBuf
import scala.util.Try

class ForwardOpenService(request: ForwardOpenRequest) extends AbstractInvokableService[ForwardOpenResponse] {

  private val requestPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = ForwardOpen.ServiceCode,
      requestPath = requestPath,
      requestData = ForwardOpenRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  def decodeResponse(responseData: ByteBuf): Try[ForwardOpenResponse] = {
    ForwardOpenResponse.decode(responseData)
  }

}
