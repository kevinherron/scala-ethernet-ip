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
