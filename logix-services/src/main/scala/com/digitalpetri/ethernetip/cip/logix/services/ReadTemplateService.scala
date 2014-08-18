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
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.logix._
import com.digitalpetri.ethernetip.cip.logix.services.ReadTemplateService.{ReadTemplateServiceRequest, ReadTemplateServiceResponse}
import com.digitalpetri.ethernetip.cip.structs.{MessageRouterRequest, MessageRouterResponse}
import com.digitalpetri.ethernetip.client.cip.services.AbstractMultiInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.{ByteBufUtil, ByteBuf}
import io.netty.util.ReferenceCountUtil

class ReadTemplateService(attributes: TemplateAttributes, requestPath: PaddedEPath)
  extends AbstractMultiInvokableService[ReadTemplateServiceResponse] with StrictLogging {

  private val buffers = new AtomicReference[Seq[ByteBuf]](Seq.empty)

  def getRequestData: ByteBuf = buildRequestData()

  private def buildRequestData(offset: Int = 0): ByteBuf = {
    var bytesToRead = (attributes.objectDefinitionSize * 4) - 23
    bytesToRead -= totalBytesRead()
    bytesToRead = round(bytesToRead, 4) + 4

    val request = ReadTemplateServiceRequest(offset, bytesToRead)

    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.ReadTemplate,
      requestPath = requestPath,
      requestData = ReadTemplateServiceRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  private def totalBytesRead(): Int = {
    buffers.get().foldLeft(0)((total, b) => total + b.readableBytes())
  }

  private def round(m: Int, n: Int): Int = {
    ((m + n - 1) / n) * n
  }

  /**
   * A [[MessageRouterResponse]] containing response data was received with a general status of either 0x00 (Success)
   * or 0x06 (Partial Data). Decode the data contained in the response and return either the next request data or a
   * decoded service response.
   * @param routerResponse The [[MessageRouterResponse]] containing the service response data.
   * @return Either the next request data or the decoded response.
   */
  def decodeResponse(routerResponse: MessageRouterResponse): Either[ByteBuf, ReadTemplateServiceResponse] = {
    routerResponse.data match {
      case Some(data) =>
        buffers.set(buffers.get() :+ data.slice())

        if (routerResponse.generalStatus == 0x06) {
          Left(buildRequestData(totalBytesRead()))
        } else {
          Right(decodeResponse())
        }

      case None => Right(decodeResponse())
    }
  }

  private def decodeResponse(): ReadTemplateServiceResponse = {
    val composite = Buffers.composite()
      .addComponents(buffers.get(): _*)
      .writerIndex(totalBytesRead())

    val response = ReadTemplateServiceResponse.decode(
      composite.order(ByteOrder.LITTLE_ENDIAN), attributes)

    ReferenceCountUtil.release(composite)

    response
  }

}


object ReadTemplateService {

  case class ReadTemplateServiceRequest(offset: Int, bytesToRead: Int)

  case class ReadTemplateServiceResponse(template: TemplateInstance)


  object ReadTemplateServiceRequest {

    def encode(request: ReadTemplateServiceRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeInt(request.offset)
      buffer.writeShort(request.bytesToRead)
    }

    def decode(buffer: ByteBuf): ReadTemplateServiceRequest = {
      ??? // TODO
    }

  }

  object ReadTemplateServiceResponse {

    def encode(response: ReadTemplateServiceResponse, buffer: ByteBuf = Buffers.unpooled()) {

    }

    def decode(buffer: ByteBuf, attributes: TemplateAttributes): ReadTemplateServiceResponse = {
      val partialMembers = for {
        i <- 0 until attributes.memberCount
      } yield {
        val infoWord    = buffer.readShort()
        val symbolType  = SymbolType.decode(buffer)
        val offset      = buffer.readUnsignedInt().toInt

        TemplateMember(_: String, infoWord, symbolType, offset)
      }

      val templateName = readNullTerminatedString(buffer).split(";")(0)

      val members = partialMembers.zipWithIndex.map {
        case (member, i) =>
          var name = readNullTerminatedString(buffer)
          if (name.isEmpty) name = s"__UnnamedMember$i"

          member(name)
      }

      ReadTemplateServiceResponse(TemplateInstance(templateName, attributes, members))
    }

  }

  private final def readNullTerminatedString(buffer: ByteBuf): String = {
    val length = buffer.bytesBefore(0)

    if (length != -1) {
      val s = buffer.toString(buffer.readerIndex(), length, Charset.forName("US-ASCII"))
      buffer.skipBytes(length + 1)
      s
    } else {
      ""
    }
  }

}
