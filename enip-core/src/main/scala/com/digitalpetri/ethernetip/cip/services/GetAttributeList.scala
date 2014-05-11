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
import io.netty.buffer.{ByteBufUtil, ByteBuf}
import scala.util.Try

object GetAttributeList {

  case class GetAttributeListRequest(attributes: Seq[AttributeRequest])
  case class GetAttributeListResponse(attributes: Seq[AttributeResponse])

  type AttributeRequest = Int
//  /**
//   * @param id Attribute identifier.
//   */
//  case class AttributeRequest(id: Int) {
//    override def toString: String = s"$productPrefix(id=$id)"
//  }

  /**
   * @param id Attribute identifier.
   * @param status Status of the attribute response. 0x00 == Success.
   * @param data Attribute response. Only exists when status == 0x00.
   */
  case class AttributeResponse(id: Int, status: Int, data: Option[ByteBuf]) {
    override def toString: String = f"$productPrefix(id=$id, status=0x$status%02X, data=${data.map(ByteBufUtil.hexDump)})"
  }

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
        i <- 0 until count
      } yield AttributeResponse.decode(attributeSizes(i), buffer)

      GetAttributeListResponse(attributes)
    }
  }

  object AttributeRequest {
    def encode(request: AttributeRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(request)
    }

    def decode(buffer: ByteBuf): AttributeRequest = {
      buffer.readUnsignedShort()
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
        else Some(buffer.readSlice(size))
      }

      AttributeResponse(id, status, data)
    }
  }

}
