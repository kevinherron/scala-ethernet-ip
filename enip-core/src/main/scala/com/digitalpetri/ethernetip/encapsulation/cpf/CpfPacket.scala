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

package com.digitalpetri.ethernetip.encapsulation.cpf

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * The common packet format (CPF) defines a standard format for protocol packets that are transported with the
 * encapsulation protocol. The common packet format is a general-purpose mechanism designed to accommodate future packet
 * or address types.
 *
 * The common packet format shall consist of an item count, followed by a number of items. Some items are classified as
 * “address items” (carries addressing information) or “data items” (carries encapsulated data). The number of items to
 * be included depends on the encapsulation command and usage of the command.
 *
 * @param items [[CpfItem]]s to be included in the packet.
 */
case class CpfPacket(items: Seq[CpfItem])

object CpfPacket {

  def encode(packet: CpfPacket, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    if (packet.items.isEmpty) return buffer

    buffer.writeShort(packet.items.size)

    packet.items.foreach(CpfItem.encode(_, buffer))

    buffer
  }

  def decode(buffer: ByteBuf): CpfPacket = {
    val itemCount = buffer.readUnsignedShort()

    def decodeItems(buffer: ByteBuf, items: List[CpfItem], itemCount: Int): List[CpfItem] = {
      if (itemCount == 0) items
      else decodeItems(buffer, items :+ CpfItem.decode(buffer), itemCount - 1)
    }

    CpfPacket(decodeItems(buffer, List.empty[CpfItem], itemCount))
  }

}

