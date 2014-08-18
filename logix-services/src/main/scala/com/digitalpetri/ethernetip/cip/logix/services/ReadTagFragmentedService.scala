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

import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.logix.services.ReadTagFragmentedService.{ReadTagFragmentedRequest, ReadTagFragmentedResponse}
import com.digitalpetri.ethernetip.cip.logix.{CipStructure, LogixServiceCodes, TagType}
import com.digitalpetri.ethernetip.cip.structs.{MessageRouterRequest, MessageRouterResponse}
import com.digitalpetri.ethernetip.client.cip.services.AbstractMultiInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

class ReadTagFragmentedService(request: ReadTagFragmentedRequest,
                               requestPath: PaddedEPath) extends AbstractMultiInvokableService[ReadTagFragmentedResponse] {

  private val headerBuffer = new AtomicReference[ByteBuf]()
  private val dataBuffers = new AtomicReference[Seq[ByteBuf]](Seq.empty)

  override def getRequestData: ByteBuf = buildRequestData()

  private def buildRequestData(offset: Int = 0): ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.ReadTagFragmented,
      requestPath = requestPath,
      requestData = ReadTagFragmentedRequest.encode(request.copy(offset = offset)))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * A [[MessageRouterResponse]] containing response data was received with a general status of either 0x00 (Success)
   * or 0x06 (Partial Data). Decode the data contained in the response and return either the next request data or a
   * decoded service response.
   * @param routerResponse The [[MessageRouterResponse]] containing the service response data.
   * @return Either the next request data or the decoded response.
   */
  override def decodeResponse(routerResponse: MessageRouterResponse): Either[ByteBuf, ReadTagFragmentedResponse] = {
    routerResponse.data match {
      case Some(data) =>
        val structured = data.getShort(data.readerIndex()) == 0x02A0

        if (headerBuffer.get() == null) {
          if (structured) headerBuffer.set(data.readSlice(4).retain())
          else headerBuffer.set(data.readSlice(2).retain())
        } else {
          if (structured) data.skipBytes(4)
          else data.skipBytes(2)
        }

        dataBuffers.set(dataBuffers.get() :+ data.slice().retain())

        if (routerResponse.generalStatus == 0x06) {
          Left(buildRequestData(totalDataBytesRead()))
        } else {
          Right(decodeResponse())
        }

      case None => Right(decodeResponse())
    }
  }

  private def totalDataBytesRead(): Int = {
    dataBuffers.get().foldLeft(0)((total, b) => total + b.readableBytes())
  }

  private def decodeResponse(): ReadTagFragmentedResponse = {
    val writerIndex = headerBuffer.get().readableBytes() + totalDataBytesRead()

    val composite = Buffers.composite()
      .addComponent(headerBuffer.get())
      .addComponents(dataBuffers.get(): _*)
      .writerIndex(writerIndex)
      .order(ByteOrder.LITTLE_ENDIAN)

    val response = ReadTagFragmentedResponse.decode(composite)
    composite.release()
    response
  }

}

object ReadTagFragmentedService {

  case class ReadTagFragmentedRequest(elements: Int = 1, offset: Long = 0L)
  case class ReadTagFragmentedResponse(tagType: TagType, tagData: ByteBuf)

  object ReadTagFragmentedRequest {
    def encode(request: ReadTagFragmentedRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request.elements)
      buffer.writeInt(request.offset.toInt)
    }
  }

  object ReadTagFragmentedResponse {
    def decode(buffer: ByteBuf): ReadTagFragmentedResponse = {
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

      ReadTagFragmentedResponse(tagType, tagData)
    }
  }

}

