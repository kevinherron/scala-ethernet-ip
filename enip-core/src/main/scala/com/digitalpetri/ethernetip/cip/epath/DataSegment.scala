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

package com.digitalpetri.ethernetip.cip.epath

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

sealed abstract class DataSegment

object DataSegment {
  val SegmentType = 0x80
}

case class AnsiDataSegment(data: String) extends DataSegment
case class SimpleDataSegment(data: Seq[Short])

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
