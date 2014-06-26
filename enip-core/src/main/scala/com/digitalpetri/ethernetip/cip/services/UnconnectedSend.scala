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
import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.util.{Buffers, TimeoutCalculator}
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
