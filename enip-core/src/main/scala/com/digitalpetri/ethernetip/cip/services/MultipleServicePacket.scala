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

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath.{InstanceId, ClassId, PaddedEPath}
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

object MultipleServicePacket {

  val RequestPath = PaddedEPath(
    ClassId(CipClassCodes.MessageRouterObject),
    InstanceId(0x01))

  case class MultipleServicePacketRequest(requests: Seq[ByteBuf])
  case class MultipleServicePacketResponse(responses: Seq[ByteBuf])

  object MultipleServicePacketRequest {

    def encode(request: MultipleServicePacketRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer // TODO
    }

    def decode(buffer: ByteBuf): MultipleServicePacketRequest = {
      ??? // TODO
    }

  }

  object MultipleServicePacketResponse {

    def encode(request: MultipleServicePacketResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer // TODO
    }

    def decode(buffer: ByteBuf): MultipleServicePacketResponse = {
      ??? // TODO
    }

  }
}
