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

