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
import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.util.{TimeoutCalculator, Buffers}
import io.netty.buffer.ByteBuf
import scala.concurrent.duration.Duration

object UnconnectedSend {

  val ServiceCode = 0x52

  val ConnectionManagerPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  case class UnconnectedSendRequest(timeout: Duration, embeddedRequest: ByteBuf, connectionPath: PaddedEPath)

  object UnconnectedSendRequest {

    def encode(request: UnconnectedSendRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      val priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(request.timeout)

      // priority/timeTick & timeoutTicks
      buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF)
      buffer.writeByte(priorityAndTimeoutBytes >> 0 & 0xFF)

      // message length + message
      val bytesWritten = encodeEmbeddedMessage(request, buffer)

      // pad byte if length was odd
      if (bytesWritten % 2 != 0) buffer.writeByte(0x00)

      // path length + reserved + path
      encodeConnectionPath(request, buffer)

      buffer
    }

    private def encodeEmbeddedMessage(request: UnconnectedSendRequest, buffer: ByteBuf): Int = {
      // length of embedded message
      val lengthStartIndex = buffer.writerIndex()
      buffer.writeShort(0)

      // embedded message
      val messageStartIndex = buffer.writerIndex()
      buffer.writeBytes(request.embeddedRequest)

      // go back and update length
      val bytesWritten = buffer.writerIndex() - messageStartIndex
      buffer.markWriterIndex()
      buffer.writerIndex(lengthStartIndex)
      buffer.writeShort(bytesWritten)
      buffer.resetWriterIndex()

      bytesWritten
    }

    private def encodeConnectionPath(request: UnconnectedSendRequest, buffer: ByteBuf) {
      // connectionPath length
      val pathLengthStartIndex = buffer.writerIndex()
      buffer.writeByte(0)

      // reserved byte
      buffer.writeByte(0x00)

      // encode the path segments...
      val pathDataStartIndex = buffer.writerIndex()

      request.connectionPath.segments.foreach {
        case s: LogicalSegment[_] => LogicalSegment.encode(s, padded = true, buffer)
        case s: PortSegment       => PortSegment.encode(s, buffer)
      }

      // go back and update the length.
      val pathBytesWritten = buffer.writerIndex() - pathDataStartIndex
      val wordsWritten = pathBytesWritten / 2
      buffer.markWriterIndex()
      buffer.writerIndex(pathLengthStartIndex)
      buffer.writeByte(wordsWritten.asInstanceOf[Byte])
      buffer.resetWriterIndex()
    }

  }

}
