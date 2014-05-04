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

  case class MultipleServicePacketRequest(serviceRequests: Seq[ByteBuf])
  case class MultipleServicePacketResponse(serviceResponses: Seq[ByteBuf])

  object MultipleServicePacketRequest {

    def encode(request: MultipleServicePacketRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      val serviceRequests = request.serviceRequests

      buffer.writeShort(serviceRequests.length)

      val offsets = serviceRequests.foldLeft(Seq[Int](0 + 2 + 2 * serviceRequests.length)) {
        (offsets, data) => offsets :+ offsets.last + data.readableBytes()
      }
      offsets.dropRight(1).foreach(offset => buffer.writeShort(offset))

      serviceRequests.foreach(buffer.writeBytes)

      buffer
    }

    def decode(buffer: ByteBuf): MultipleServicePacketRequest = {
      val dataStartIndex  = buffer.readerIndex()
      val serviceCount    = buffer.readUnsignedShort()
      val offsets         = for (i <- 0 until serviceCount) yield buffer.readUnsignedShort()

      def slices(offsets: Seq[Int], data: Seq[ByteBuf]): Seq[ByteBuf] = {
        offsets match {
          case Seq(offset) =>
            data :+ sliceAndSkip(buffer, dataStartIndex + offset, buffer.readableBytes())

          case Seq(offset, tail@_*) =>
            slices(tail, data :+ sliceAndSkip(buffer, dataStartIndex + offset, tail.head - offset))
        }
      }

      MultipleServicePacketRequest(slices(offsets, Seq.empty))
    }

  }

  object MultipleServicePacketResponse {

    def encode(request: MultipleServicePacketResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      val serviceResponses = request.serviceResponses

      buffer.writeShort(serviceResponses.length)

      val offsets = serviceResponses.foldLeft(Seq[Int](0 + 2 + 2 * serviceResponses.length)) {
        (offsets, data) => offsets :+ offsets.last + data.readableBytes()
      }
      offsets.dropRight(1).foreach(offset => buffer.writeShort(offset))

      serviceResponses.foreach(buffer.writeBytes)

      buffer
    }

    def decode(buffer: ByteBuf): MultipleServicePacketResponse = {
      val dataStartIndex  = buffer.readerIndex()
      val serviceCount    = buffer.readUnsignedShort()
      val offsets         = for (i <- 0 until serviceCount) yield buffer.readUnsignedShort()

      def slices(offsets: Seq[Int], data: Seq[ByteBuf]): Seq[ByteBuf] = {
        offsets match {
          case Seq(offset) =>
            data :+ sliceAndSkip(buffer, dataStartIndex + offset, buffer.readableBytes())

          case Seq(offset, tail@_*) =>
            slices(tail, data :+ sliceAndSkip(buffer, dataStartIndex + offset, tail.head - offset))
        }
      }

      MultipleServicePacketResponse(slices(offsets, List.empty))
    }

  }

  private def sliceAndSkip(buffer: ByteBuf, index: Int, length: Int): ByteBuf = {
    val slice = buffer.slice(index, length)
    buffer.skipBytes(length)
    slice
  }

}
