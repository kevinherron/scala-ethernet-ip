/*
 * Copyright 2014 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.ethernetip.cip.structs

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import scala.util.Try

/**
 * @param serviceCode Service code of the request.
 * @param requestPath The request/application path.
 * @param requestData Service specific data per object definition to be delivered in the Explicit Messaging Request.
 *                    If no additional data is to be sent with the Explicit Messaging Request, then this array will be
 *                    empty.
 */
case class MessageRouterRequest(serviceCode: Int, requestPath: PaddedEPath, requestData: ByteBuf)

object MessageRouterRequest {

  def encode(request: MessageRouterRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeByte(request.serviceCode)
    PaddedEPath.encode(request.requestPath, buffer)
    buffer.writeBytes(request.requestData)

    buffer
  }

  def decode(buffer: ByteBuf): MessageRouterRequest = {
    MessageRouterRequest(
      serviceCode = buffer.readUnsignedByte(),
      requestPath = PaddedEPath.decode(buffer),
      requestData = buffer.slice())
  }

}

/**
 * @param serviceCode The request service code + 0x80.
 * @param generalStatus One of the general status codes defined in the CIP specification appendix B.
 * @param additionalStatus Additional status codes.
 * @param data Response data.
 */
case class MessageRouterResponse(serviceCode: Int,
                                 generalStatus: Short,
                                 additionalStatus: Seq[Short],
                                 data: Option[ByteBuf])

object MessageRouterResponse {

  def encode(response: MessageRouterResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeByte(response.serviceCode)
    buffer.writeByte(0x00)
    buffer.writeByte(response.generalStatus)
    buffer.writeByte(response.additionalStatus.size)
    response.additionalStatus.foreach(s => buffer.writeShort(s))
    response.data.foreach(buffer.writeBytes)

    buffer
  }

  def decode(buffer: ByteBuf): Try[MessageRouterResponse] = Try {
    val replyService  = buffer.readUnsignedByte()
    val reserved      = buffer.readByte()
    val generalStatus = buffer.readUnsignedByte()

    assert(reserved == 0)

    def decodeAdditionalStatus(additional: List[Short], count: Int): List[Short] = {
      if (count == 0) additional
      else decodeAdditionalStatus(additional :+ buffer.readShort(), count - 1)
    }

    val additionalStatus = decodeAdditionalStatus(List.empty, buffer.readUnsignedByte())

    val data: Option[ByteBuf] = {
      if (buffer.readableBytes() == 0) None
      else Some(buffer.readSlice(buffer.readableBytes()))
    }

    MessageRouterResponse(replyService, generalStatus, additionalStatus, data)
  }

}
