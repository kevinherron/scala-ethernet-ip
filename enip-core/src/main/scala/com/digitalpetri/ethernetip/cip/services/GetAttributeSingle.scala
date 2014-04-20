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

package com.digitalpetri.ethernetip.cip.services

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.{ByteBufUtil, ByteBuf}
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
