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
import com.digitalpetri.ethernetip.cip.logix.services.ReadTagService.{ReadTagRequest, ReadTagResponse}
import com.digitalpetri.ethernetip.cip.logix.{CipStructure, LogixServiceCodes, TagType}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import com.digitalpetri.ethernetip.client.cip.services.AbstractInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.{ByteBuf, ByteBufUtil}

import scala.util.Try

class ReadTagService(request: ReadTagRequest,
                     requestPath: PaddedEPath) extends AbstractInvokableService[ReadTagResponse] with StrictLogging {

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.ReadTag,
      requestPath = requestPath,
      requestData = ReadTagRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  def decodeResponse(responseData: ByteBuf): Try[ReadTagResponse] = {
    ReadTagResponse.decode(responseData)
  }

}

object ReadTagService {

  case class ReadTagRequest(elements: Int = 1)

  case class ReadTagResponse(tagType: TagType, tagData: ByteBuf) {
    override def toString: String = {
      productPrefix + {
        if (tagData.readableBytes() > 4) s"($tagType, ByteBuf[${tagData.readableBytes()}])"
        else s"($tagType, ${ByteBufUtil.hexDump(tagData)})"
      }
    }
  }

  object ReadTagRequest {

    def encode(request: ReadTagRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request.elements)
    }

    def decode(buffer: ByteBuf): Try[ReadTagRequest] = Try {
      val elements = buffer.readUnsignedShort()

      ReadTagRequest(elements)
    }

  }

  object ReadTagResponse {

    def encode(response: ReadTagResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      response.tagType match {
        case CipStructure(_) =>
          buffer.writeShort(0x02A0)
          TagType.encode(response.tagType, buffer)

        case _ =>
          TagType.encode(response.tagType, buffer)
      }

      buffer.writeBytes(response.tagData)
    }

    def decode(buffer: ByteBuf): Try[ReadTagResponse] = Try {
      val tagType: TagType = {
        if (buffer.getShort(buffer.readerIndex()) == 0x02A0) {
          buffer.skipBytes(2)
          CipStructure(buffer.readShort())
        } else {
          TagType.decode(buffer)
        }
      }

//      val tagData = buffer.readSlice(buffer.readableBytes())
      val tagData = buffer.copy()

      ReadTagResponse(tagType, tagData)
    }

  }

}
