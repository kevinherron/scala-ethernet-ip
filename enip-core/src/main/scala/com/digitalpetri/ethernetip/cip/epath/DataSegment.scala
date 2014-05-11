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

sealed abstract class DataSegment extends EPathSegment

object DataSegment {
  val SegmentType = 0x80
}

case class AnsiDataSegment(data: String) extends DataSegment
case class SimpleDataSegment(data: Seq[Short]) extends DataSegment

object AnsiDataSegment {

  val TypeByte = 0x91 // SegmentType + Ansi sub-type

  def encode(segment: AnsiDataSegment, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    val data: String = {
      if (segment.data.length <= 255) segment.data
      else segment.data.substring(0, 255)
    }

    buffer.writeByte(TypeByte)
    buffer.writeByte(data.length)
    buffer.writeBytes(data.getBytes("ASCII"))
    if (data.length % 2 != 0) buffer.writeByte(0)

    buffer
  }

  def decode(buffer: ByteBuf): AnsiDataSegment = {
    val typeByte = buffer.readUnsignedByte()
    assert(typeByte == TypeByte)

    val length = buffer.readUnsignedByte()
    assert(length >= 0 && length <= 255)

    val bytes = buffer.readBytes(length)
    if (length % 2 != 0) buffer.skipBytes(1)

    AnsiDataSegment(new String(bytes.array(), "ASCII"))
  }

}

object SimpleDataSegment {

  val TypeByte = 0x80 // SegmentType + Simple sub-type

  def encode(segment: SimpleDataSegment, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeByte(TypeByte)
    buffer.writeByte(segment.data.length)
    segment.data.foreach(buffer.writeShort(_))

    buffer
  }

  def decode(buffer: ByteBuf): SimpleDataSegment = {
    val typeByte = buffer.readUnsignedByte()
    assert(typeByte == TypeByte)

    val length = buffer.readUnsignedByte()
    assert(length >= 0 && length <= 255)

    def readData(words: Seq[Short], remaining: Int): Seq[Short] = {
      if (remaining == 0) words
      else readData(words :+ buffer.readShort(), remaining - 1)
    }

    val data = readData(Seq.empty, length)

    SimpleDataSegment(data)
  }

}
