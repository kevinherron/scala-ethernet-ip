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

package com.digitalpetri.ethernetip.cip.services

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.{ByteBuf, ByteBufUtil}

import scala.util.Try

object GetAttributeSingle {

  case class GetAttributeSingleRequest(attributeId: Int)

  case class GetAttributeSingleResponse(attributeData: ByteBuf) {
    override def toString: String = s"$productPrefix(${ByteBufUtil.hexDump(attributeData)})"
  }

  object GetAttributeSingleRequest {
    def encode(request: GetAttributeSingleRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeByte(request.attributeId)
    }

    def decode(buffer: ByteBuf): Try[GetAttributeSingleResponse] = Try {
      GetAttributeSingleResponse(buffer.readSlice(buffer.readableBytes()))
    }
  }

  object GetAttributeSingleResponse {
    def encode(response: GetAttributeSingleResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeBytes(response.attributeData);
    }

    def decode(buffer: ByteBuf): Try[GetAttributeSingleResponse] = Try {
      GetAttributeSingleResponse(buffer.readSlice(buffer.readableBytes()))
    }
  }

}
