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

package com.digitalpetri.ethernetip.cip.epath

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

sealed abstract class EPath {
  def segments: Seq[EPathSegment]

  override def toString: String = {
    s"${getClass.getSimpleName}(${segments.mkString(",")})"
  }
}

abstract class EPathSegment

object EPathSegment {

  def encode(segment: EPathSegment, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    ??? // TODO
  }

  def decode(buffer: ByteBuf): EPathSegment = {
    ??? // TODO
  }

}

case class PackedEPath(segments: EPathSegment*) extends EPath
case class PaddedEPath(segments: EPathSegment*) extends EPath

object PackedEPath {
  def encode(path: PackedEPath, buffer: ByteBuf) {
    // TODO
  }

  def decode(buffer: ByteBuf): PackedEPath = {
    ??? // TODO
  }
}

object PaddedEPath {

  def encode(path: PaddedEPath, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    // length placeholder...
    val lengthStartIndex = buffer.writerIndex()
    buffer.writeByte(0)

    // encode the path segments...
    val dataStartIndex = buffer.writerIndex()

    path.segments.foreach {
      case s: LogicalSegment[_] => LogicalSegment.encode(s, padded = true, buffer)
      case s: PortSegment       => PortSegment.encode(s, buffer)
      case s: AnsiDataSegment   => AnsiDataSegment.encode(s, buffer)
      case s: SimpleDataSegment => SimpleDataSegment.encode(s, buffer)
    }

    // go back and update the length
    val bytesWritten = buffer.writerIndex() - dataStartIndex
    val wordsWritten = bytesWritten / 2
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeByte(wordsWritten.asInstanceOf[Short])
    buffer.resetWriterIndex()
  }

  def decode(buffer: ByteBuf): PaddedEPath = {
    val wordCount = buffer.readUnsignedByte()
    val byteCount = wordCount * 2

    val dataStartIndex = buffer.readerIndex()

    def decodeSegments(segments: Seq[EPathSegment] = Seq.empty): Seq[EPathSegment] = {
      if (buffer.readerIndex() >= dataStartIndex + byteCount) segments
      else decodeSegments(segments :+ EPathSegment.decode(buffer))
    }

    PaddedEPath(decodeSegments(): _*)
  }
  
}
