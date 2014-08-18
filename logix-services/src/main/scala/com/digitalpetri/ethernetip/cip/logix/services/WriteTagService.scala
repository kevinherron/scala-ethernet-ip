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

package com.digitalpetri.ethernetip.cip.logix.services

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.logix.services.WriteTagService.WriteTagRequest
import com.digitalpetri.ethernetip.cip.logix.{CipStructure, LogixServiceCodes, TagType}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import com.digitalpetri.ethernetip.client.cip.services.AbstractInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

import scala.util.Try

class WriteTagService(request: WriteTagRequest, requestPath: PaddedEPath) extends AbstractInvokableService[Unit] {

  override def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.WriteTag,
      requestPath = requestPath,
      requestData = WriteTagRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  override def decodeResponse(responseData: ByteBuf): Try[Unit] = Try(Unit)

}

object WriteTagService {

  case class WriteTagRequest(tagType: TagType, elements: Int, data: ByteBuf)

  object WriteTagRequest {

    def encode(request: WriteTagRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      request.tagType match {
        case CipStructure(_) =>
          buffer.writeByte(0xA0).writeByte(0x02)
          TagType.encode(request.tagType, buffer)
        case _ =>
          TagType.encode(request.tagType, buffer)
      }

      buffer.writeShort(request.elements)
      buffer.writeBytes(request.data)
    }

    def decode(buffer: ByteBuf): WriteTagRequest = {
      WriteTagRequest(
        tagType   = TagType.decode(buffer),
        elements  = buffer.readUnsignedShort(),
        data      = buffer.readSlice(buffer.readableBytes()))
    }

  }

}
