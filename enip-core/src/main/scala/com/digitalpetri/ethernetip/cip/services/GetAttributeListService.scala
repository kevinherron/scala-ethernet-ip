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
import io.netty.buffer.ByteBuf
import scala.util.Try

object GetAttributeListService {

  case class GetAttributeListRequest(attributes: Seq[AttributeRequest])
  case class GetAttributeListResponse(attributes: Seq[AttributeResponse])

  /**
   * @param id Attribute identifier.
   */
  case class AttributeRequest(id: Int)

  /**
   * @param id Attribute identifier.
   * @param status Status of the attribute response. 0x00 == Success.
   * @param data Attribute response. Only exists when status == 0x00.
   */
  case class AttributeResponse(id: Int, status: Int, data: Option[ByteBuf])

  object GetAttributeListRequest {
    def encode(request: GetAttributeListRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request.attributes.size)
      request.attributes.foreach(AttributeRequest.encode(_, buffer))

      buffer
    }

    def decode(buffer: ByteBuf): Try[GetAttributeListRequest] = Try {
      val count = buffer.readUnsignedShort()

      val attributes = for (i <- 0 to count) yield AttributeRequest.decode(buffer)

      GetAttributeListRequest(attributes)
    }
  }

  object GetAttributeListResponse {
    def encode(response: GetAttributeListResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(response.attributes.size)
      response.attributes.foreach(AttributeResponse.encode(_, buffer))

      buffer
    }

    def decode(attributeSizes: Seq[Int], buffer: ByteBuf): Try[GetAttributeListResponse] = Try {
      val count = buffer.readUnsignedShort()

      val attributes = for {
        i <- 0 to count
      } yield AttributeResponse.decode(attributeSizes(i), buffer)

      GetAttributeListResponse(attributes)
    }
  }

  object AttributeRequest {
    def encode(request: AttributeRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request.id)
    }

    def decode(buffer: ByteBuf): AttributeRequest = {
      AttributeRequest(buffer.readUnsignedShort())
    }
  }

  object AttributeResponse {
    def encode(response: AttributeResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(response.id)
      buffer.writeShort(response.status)
      response.data.map(buffer.writeBytes)

      buffer
    }

    def decode(size: Int, buffer: ByteBuf): AttributeResponse = {
      val id = buffer.readUnsignedShort()
      val status = buffer.readUnsignedShort()

      val data: Option[ByteBuf] = {
        if (status != 0x00) None
        else Some(buffer.readBytes(size))
      }

      AttributeResponse(id, status, data)
    }
  }

}
