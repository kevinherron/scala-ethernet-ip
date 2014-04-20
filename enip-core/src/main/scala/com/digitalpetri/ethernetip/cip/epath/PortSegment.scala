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

case class PortSegment(portId: Int, linkAddress: Array[Byte] = Array.emptyByteArray) extends EPathSegment

object PortSegment {

  private val ExtendedLinkAddressSize = 1 << 4

  def encode(segment: PortSegment, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    val writerIndex         = buffer.writerIndex()
    val linkAddressLength   = segment.linkAddress.length
    val needLinkAddressSize = linkAddressLength > 1
    val needExtendedPort    = segment.portId > 14

    var segmentByte = if (needExtendedPort) 0x0F else segment.portId
    if (needLinkAddressSize) segmentByte |= ExtendedLinkAddressSize
    buffer.writeByte(segmentByte)

    if (needLinkAddressSize) buffer.writeByte(linkAddressLength)
    if (needExtendedPort) buffer.writeShort(segment.portId)
    buffer.writeBytes(segment.linkAddress)

    val bytesWritten = buffer.writerIndex() - writerIndex
    if (bytesWritten % 2 != 0) buffer.writeByte(0)

    buffer
  }

  def decode(buffer: ByteBuf): PortSegment = {
    val segmentByte = buffer.readUnsignedByte()
    val extendedLink = ((segmentByte >> 4) & 1) == 1

    val linkSize = {
      if (extendedLink) buffer.readUnsignedByte()
      else 1
    }

    val portId = {
      if ((segmentByte & 0x0F) == 0x0F) buffer.readUnsignedShort()
      else segmentByte & 0x0F
    }

    val linkAddress = new Array[Byte](linkSize)
    buffer.readBytes(linkAddress)

    PortSegment(portId, linkAddress)
  }

}
