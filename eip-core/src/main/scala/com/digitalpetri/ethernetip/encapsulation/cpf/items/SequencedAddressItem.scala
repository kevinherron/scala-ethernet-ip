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

package com.digitalpetri.ethernetip.encapsulation.cpf.items

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfItem
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * This address item shall be used for CIP transport class 0 and class 1 connected data. The data shall contain a
 * connection identifier and a sequence number.
 * 
 * @param connectionId Connection identifier.
 * @param sequenceNumber Sequence number.
 */
case class SequencedAddressItem(connectionId: Long, sequenceNumber: Long) extends CpfItem(SequencedAddressItem.TypeId)

object SequencedAddressItem {

  val TypeId = 0x8002
  val Length = 8

  def encode(item: SequencedAddressItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)
    buffer.writeInt(item.connectionId.toInt)
    buffer.writeInt(item.sequenceNumber.toInt)
  }

  def decode(buffer: ByteBuf): SequencedAddressItem = {
    val typeId          = buffer.readUnsignedShort()
    val length          = buffer.readUnsignedShort()
    val connectionId    = buffer.readUnsignedInt()
    val sequenceNumber  = buffer.readUnsignedInt()

    assert(typeId == TypeId)
    assert(length == Length)

    SequencedAddressItem(connectionId, sequenceNumber)
  }

}
