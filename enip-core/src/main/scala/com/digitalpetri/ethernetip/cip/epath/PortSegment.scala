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
