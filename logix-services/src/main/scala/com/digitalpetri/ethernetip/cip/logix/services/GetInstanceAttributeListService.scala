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

import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference

import com.digitalpetri.ethernetip.cip.epath.{AnsiDataSegment, ClassId, InstanceId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.logix.services.GetInstanceAttributeListService.{GetInstanceAttributeListRequest, GetInstanceAttributeListResponse}
import com.digitalpetri.ethernetip.cip.logix.{LogixClassCodes, LogixServiceCodes, SymbolInstance, SymbolType}
import com.digitalpetri.ethernetip.cip.structs.{MessageRouterRequest, MessageRouterResponse}
import com.digitalpetri.ethernetip.client.cip.services.AbstractMultiInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

class GetInstanceAttributeListService(program: Option[String]) extends AbstractMultiInvokableService[GetInstanceAttributeListResponse] {

  private val symbols = new AtomicReference[Seq[SymbolInstance]](Seq.empty)

  def getRequestData: ByteBuf = buildRequestData(0)

  private def buildRequestData(id: Int): ByteBuf = {
    val requestPath = program match {
      case Some(p)  => PaddedEPath(AnsiDataSegment(p), ClassId(LogixClassCodes.Symbol), InstanceId(id))
      case None     => PaddedEPath(ClassId(LogixClassCodes.Symbol), InstanceId(id))
    }

    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.GetInstanceAttributeList,
      requestPath = requestPath,
      requestData = GetInstanceAttributeListRequest.encode(GetInstanceAttributeListRequest()))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * A [[MessageRouterResponse]] containing response data was received with a general status of either 0x00 (Success)
   * or 0x06 (Partial Data). Decode the data contained in the response and return either the next request data or a
   * decoded service response.
   * @param routerResponse The [[MessageRouterResponse]] containing the service response data.
   * @return Either the next request data or the decoded response.*/
  def decodeResponse(routerResponse: MessageRouterResponse): Either[ByteBuf, GetInstanceAttributeListResponse] = {
    routerResponse.data match {
      case Some(d) =>
        val response = GetInstanceAttributeListResponse.decode(d, program)

        symbols.set(symbols.get() ++ response.symbols)

        if (routerResponse.generalStatus == 0x06) {
          Left(buildRequestData(response.symbols.last.instanceId + 1))
        } else {
          Right(GetInstanceAttributeListResponse(symbols.get()))
        }

      case None => Right(GetInstanceAttributeListResponse(symbols.get()))
    }
  }

}

object GetInstanceAttributeListService {

  case class GetInstanceAttributeListRequest() {
    val attributeCount = 3
    val attributes = Seq(1, 2, 8)
  }

  case class GetInstanceAttributeListResponse(symbols: Seq[SymbolInstance])

  object GetInstanceAttributeListRequest {

    def encode(request: GetInstanceAttributeListRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request.attributeCount)
      request.attributes.foreach(a => buffer.writeShort(a))
      buffer
    }

    def decode(buffer: ByteBuf): GetInstanceAttributeListRequest = {
      val attributeCount = buffer.readShort()
      for (i <- 1 to attributeCount) buffer.readShort()

      GetInstanceAttributeListRequest()
    }

  }

  object GetInstanceAttributeListResponse {

    def encode(response: GetInstanceAttributeListResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      response.symbols.foreach {
        symbol =>
          buffer.writeInt(symbol.instanceId)
          buffer.writeShort(symbol.symbolName.length)
          buffer.writeBytes(symbol.symbolName.getBytes)
          SymbolType.encode(symbol.symbolType, buffer)
          buffer.writeInt(symbol.d1Size)
          buffer.writeInt(symbol.d2Size)
          buffer.writeInt(symbol.d3Size)
      }

      buffer
    }

    def decode(buffer: ByteBuf, program: Option[String]): GetInstanceAttributeListResponse = {
      def decodeSymbols(symbols: Seq[SymbolInstance] = Seq.empty): Seq[SymbolInstance] = {
        if (buffer.readableBytes() == 0) {
          symbols
        } else {
          val instanceId = buffer.readInt()
          val nameLength = buffer.readUnsignedShort()
          val symbolName = {
            val s = buffer.toString(buffer.readerIndex(), nameLength, Charset.forName("US-ASCII"))
            buffer.skipBytes(nameLength)
            s
          }
          val symbolType = SymbolType.decode(buffer)
          val d1Size = buffer.readInt()
          val d2Size = buffer.readInt()
          val d3Size = buffer.readInt()

          decodeSymbols(symbols :+ SymbolInstance(instanceId, symbolName, symbolType, d1Size, d2Size, d3Size, program))
        }
      }

      GetInstanceAttributeListResponse(decodeSymbols())
    }

  }

}
